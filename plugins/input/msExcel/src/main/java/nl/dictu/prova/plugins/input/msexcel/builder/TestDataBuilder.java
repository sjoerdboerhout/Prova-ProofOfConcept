package nl.dictu.prova.plugins.input.msexcel.builder;

import nl.dictu.prova.plugins.input.msexcel.reader.WorkbookReader;
import nl.dictu.prova.plugins.input.msexcel.reader.CellReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import nl.dictu.prova.Config;
import nl.dictu.prova.TestRunner;
import org.apache.poi.ss.formula.functions.Column;

/**
 * @author Hielke de Haan
 * @since 0.0.1
 */
public class TestDataBuilder
{
  private final static Logger LOGGER = LogManager.getLogger();
  private WorkbookReader workbookReader;
  private CellReader cellReader;
  private TestRunner testRunner;
  private String dateFormat = null;
  
  public TestDataBuilder()
  {
    cellReader = new CellReader();
  }
  
  public TestDataBuilder(TestRunner testRunner)
  {
      this.testRunner = testRunner;
      cellReader = new CellReader();
      
      try
      {
        //Argument for evaluateCellContent method
        this.dateFormat = testRunner.getPropertyValue(Config.PROVA_PLUGINS_INPUT_DATEFORMAT);
      }
      catch(Exception ex)
      {
      }
  }
  
  /**
   * Builds a set of test data from sheets on the given path
   * 
   * @param path
   * @return
   * @throws Exception
   */
  public Properties buildTestData(String path, String keySet) throws Exception
  {
    LOGGER.trace("Build testdata for: {}", path);
    
    Workbook workbook = new XSSFWorkbook(new File(path));
    workbookReader = new WorkbookReader(workbook);
    LinkedHashMap<String, Map<String, String>> testData = new LinkedHashMap<>();

    for(Sheet sheet : workbook)
    {
      LOGGER.trace("Sheet: {}", sheet::getSheetName);
      readWebSheet(sheet).forEach(testData::put);
    }

    LOGGER.trace("Number of found testData sets: {}", testData.size());
    
    return getAsProperties(testData, keySet);
  }
  
  /**
   * Builds a list containing sets of testdata and tests for DB and SOAP sheets
   * 
   * @param path
   * @param sheet
   * @return 
   * @throws Exception
   */
  public ArrayList<List<Properties>> buildTestDataAndTests(String path, String sheetname) throws Exception
  {
      LOGGER.trace("Build testdata and tests for: {}", sheetname);
      ArrayList<List<Properties>> testDatasets = new ArrayList<>();
      List<Properties> dataset = null;
      
      Workbook workbook = new XSSFWorkbook(new File(path));
      workbookReader = new WorkbookReader(workbook);
      Sheet sheet = null;
      
      for(Sheet sheetInWorkbook : workbook)
      {
        if(sheetInWorkbook.getSheetName().trim().contentEquals(sheetname.trim()))
        {
          sheet = sheetInWorkbook;
          break;
        }
      }
      if(sheet == null)
      {
        LOGGER.error("No sheet was found with sheetname " + sheetname.trim());
        return testDatasets;
      }
        
      //Optional horizontal testdata template reader
      if(testRunner.hasPropertyValue("prova.plugins.in.horizontalcolumns"))
      {
        if(testRunner.getPropertyValue("prova.plugins.in.horizontalcolumns").equalsIgnoreCase("true"))
        {
          Iterator<Row> rowIterator = sheet.rowIterator();

          if(rowIterator.hasNext())
          {
            Map<Integer, String> headers = readHeaderRow(rowIterator.next());

            LOGGER.trace("TestData Builder for SOAP & DB: '{}'", sheet.getSheetName());

            int i = 2;

            //Dataset needs 1 testdata row and 1 test validation row.
            //After size becomes 2 it is added to testDatasets and a new 
            //one is created.
            while(i < sheet.getLastRowNum())
            {
              if(dataset == null)
              {
                dataset = new ArrayList<>();
                dataset.add(0, readHorizontalColumn(sheet, i));
              } 
              else if (dataset.size() == 1)
              {
                dataset.add(1, readHorizontalColumn(sheet, i));
              } 
              else if (dataset.size() == 2)
              {
                testDatasets.add(dataset);
                dataset = new ArrayList<>();
                dataset.add(0, readHorizontalColumn(sheet, i));
              }
              else
              {
                LOGGER.error("Invalid amount of columns for a dataset. Column number is " + i + " and dataset size is " + dataset.size());
              }
              i++;
            }
          }
          return testDatasets;
        }
      }
      
      //Regular vertical testdata template reader
      Iterator<Row> rowIterator = sheet.rowIterator();
    
      if(rowIterator.hasNext())
      {
        Map<Integer, String> headers = readHeaderRow(rowIterator.next());
  
        LOGGER.trace("TestData Builder for SOAP & DB: '{}'", sheet.getSheetName());
  
        int i = 1;
          
        //Dataset needs 1 testdata column and 1 test validation column.
        //After size becomes 2 it is added to testDatasets and a new 
        //one is created.
        while(i <= headers.size())
        {
          if(dataset == null)
          {
            dataset = new ArrayList<>();
            dataset.add(0, readColumn(sheet, i, 2, sheet.getLastRowNum()));
          } 
          else if (dataset.size() == 1)
          {
            dataset.add(1, readColumn(sheet, i, 2, sheet.getLastRowNum()));
          } 
          else if (dataset.size() == 2)
          {
            testDatasets.add(dataset);
            dataset = new ArrayList<>();
            dataset.add(0, readColumn(sheet, i, 2, sheet.getLastRowNum()));
          }
          else
          {
            LOGGER.error("Invalid amount of columns for a dataset. Column number is " + i + " and dataset size is " + dataset.size());
          }
          i++;
        }
      }
      return testDatasets;
  }
  
  public Properties readHorizontalColumn(Sheet sheet, Integer selectedRow) throws Exception{
    
    Properties rowData = new Properties();
    Integer userRow = selectedRow++;
    
    Map<Integer, String> headers = readHeaderRow(sheet.getRow(0));
    //Remove first header "Keywords"
    headers.remove(0);
    
    Row row = sheet.getRow(selectedRow);
    
    for(Entry header : headers.entrySet())
    {
      Cell keyCell = sheet.getRow(0).getCell((Integer) header.getKey());
      String rowType = row.getRowNum() % 2 == 0 ? "data" : "tests";
      
      if (keyCell != null)
      {
        String key = workbookReader.evaluateCellContent(keyCell, dateFormat);
        LOGGER.trace("Found key: '{}' on column '{}'", key, header.getKey());
                  
        if (!key.isEmpty())
        {  
          Cell cell = row.getCell((Integer) header.getKey());
          if (cell != null)
          {
            String value = workbookReader.evaluateCellContent(cell, dateFormat);
            if(!cellReader.isKey(value))
            {
              if(value.length() > 0)
              {
                LOGGER.trace("Found value '{}' for key '{}' in {} row '{}'", value, key, rowType, userRow);
                rowData.put(key, value);
              }
              else
              {
                LOGGER.trace("Found no value for key '{}' in {} row '{}'", key, rowType, userRow);
              }
            }
            //Value is a tag, searching value.
            else
            {
              value = workbookReader.getTagName(value);

              LOGGER.trace("Property value is a property, retrieving value from collection.");
              if(testRunner.hasPropertyValue(value))
              {
                value = testRunner.getPropertyValue(value);
                if(value.length() > 0)
                {
                  LOGGER.trace("Found property value '{}' for key '{}' in {} row '{}'", value, key, rowType, userRow);
                  rowData.put(key, value);
                }
                else
                {
                  LOGGER.trace("Found no property value for key '{}' in {} row '{}'", key, rowType, userRow);
                }
              }
              else
              {
                throw new Exception("No value available for " + rowType + " tag " + value);
              }
            }
          }
        } 
        else
        {
        LOGGER.trace("Row {} is empty; skipping row", userRow);
        }
      }
      else
      {
        LOGGER.debug("Row {} is empty; skipping row", userRow);
      }
      if((Integer) header.getKey() == headers.size() - 1)
      {
        LOGGER.trace("Finished reading horizontal column");
      }
    }
    String message = selectedRow % 2 == 0 ? "Added " + rowData.size() + " testvalidation properties to dataset." : "Added " + rowData.size() + " testdata properties to dataset."; 
    LOGGER.trace(message);
    return rowData;
  }    
  
  public Properties readColumn(Sheet sheet, Integer column, Integer start, Integer end) throws Exception{
    
    Properties columnData = new Properties();
    String columnType = column % 2 == 0 ? "tests" : "data";
    
    for(int rowNum = start; rowNum <= end; rowNum++)
    {
      Row row = sheet.getRow(rowNum);
      Cell keyCell = row.getCell(0);
      if (keyCell != null)
      {
        String key = workbookReader.evaluateCellContent(keyCell, dateFormat);
        LOGGER.trace("Found key: '{}'", key);
                  
        if (!key.isEmpty())
        { 
          Cell cell = row.getCell(column);
          if (cell != null)
          {
            String value = workbookReader.evaluateCellContent(cell, dateFormat);
            if(!cellReader.isKey(value))
            {
              if(value.length() > 0)
              {
                LOGGER.trace("Found value '{}' for key '{}' in {} column '{}'", value, key, columnType, column);
                columnData.put(key, value);
              }
              else
              {
                LOGGER.trace("Found no value for key '{}' in {} column '{}'", key, columnType, column);
              }
            }
            //Value is a tag, searching value.
            else
            {
              value = value.substring(1, value.length() - 1);
              LOGGER.trace("Property value is a property, retrieving value from collection.");
              if(testRunner.hasPropertyValue(value))
              {
                value = testRunner.getPropertyValue(value);
                if(value.length() > 0)
                {
                  LOGGER.trace("Found value '{}' for key '{}' in {} column '{}'", value, key, columnType, column);
                  columnData.put(key, value);
                }
                else
                {
                  LOGGER.trace("Found no property value for key '{}' in {} column '{}'", key, columnType, column);
                }
              }
              else
              {
                throw new Exception("No value available for tag " + value);
              }
            }
          } 
          else 
          {
            LOGGER.trace("Found no value for key '{}' in {} column '{}'", key, columnType, column);
          }
        } 
        else
        {
        LOGGER.trace("Row {} is empty; skipping row", row.getRowNum());
        }
      }
      else
      {
        LOGGER.debug("Row {} is empty; skipping row", row.getRowNum());
      }
      if(rowNum == end)
      {
        LOGGER.trace("Finished reading column");
      }
    }
    String message = column % 2 == 0 ? "Added " + columnData.size() + " testdata properties to dataset." : "Added " + columnData.size() + " testvalidation properties to dataset."; 
    LOGGER.trace(message);
    return columnData;
  }    
  
  /**
   * 
   * @param sheet
   * @return
   * @throws Exception
   */
  private LinkedHashMap<String, Map<String, String>> readWebSheet(Sheet sheet) throws Exception
  {
    LinkedHashMap<String, Map<String, String>> testData = new LinkedHashMap<>();
    Iterator<Row> rowIterator = sheet.rowIterator();
    
    LOGGER.trace("TestData Builder readWebSheet: '{}'", sheet.getSheetName());
    
    if (rowIterator.hasNext())
    {
      // read headers
      Map<Integer, String> headers = readHeaderRow(rowIterator.next());

      // initialize submaps in testData map for eacht header
      for (int colNum = 1; colNum < headers.size(); colNum++)
      {
        LOGGER.trace("Create a column for test set '{}'", headers.get(colNum));
        testData.put(headers.get(colNum), new HashMap<>());
      }
      
      while (rowIterator.hasNext())
      {
        Row row = rowIterator.next();

        // column 0 contains key
        Cell keyCell = row.getCell(0);
        if (keyCell != null)
        {
          String key = workbookReader.evaluateCellContent(keyCell, dateFormat);
          LOGGER.trace("Found key: '{}'", key);
          
          if (!key.isEmpty())
          {
            // columns 1 and on contain values
            for (int colNum = 1; colNum < headers.size(); colNum++)
            {
              Cell cell = row.getCell(colNum);
              if (cell != null)
              {
                String value = workbookReader.evaluateCellContent(cell, dateFormat);
                
                // Empty values are only allowed in column 1
                // or keep all columns empty.
                if(value.isEmpty() && (colNum > 1))
                {
                  LOGGER.trace("No value found for key '{}' in column '{}'; copying from column '{}' ({})", key, headers.get(colNum), headers.get(colNum - 1), colNum);
                  testData.get(headers.get(colNum)).put(key, testData.get(headers.get(colNum - 1)).get(key));
                }
                else
                {
                  LOGGER.trace("Found value '{}' for key '{}' in column '{}'", value, key, headers.get(colNum));
                  testData.get(headers.get(colNum)).put(key, value);
                }
              }
            }
          } 
          else
          {
            LOGGER.trace("Row {} is empty; skipping row", row.getRowNum());
          }
        }
        else
        {
          LOGGER.debug("Row {} is empty; skipping row", row.getRowNum());
        }
      }
    }
    return testData;
  }

  private Map<Integer, String> readHeaderRow(Row row) throws Exception
  {
    Map<Integer, String> headers = new HashMap<>();
    for (Cell cell : row)
    {
      String cellContent = workbookReader.evaluateCellContent(cell, dateFormat);
      if (!cellContent.isEmpty())
        headers.put(cell.getColumnIndex(), cellContent);
    }

    if (headers.size() < 2)
      throw new Exception("No data columns found");

    LOGGER.trace("Read headers {}", headers);
    return headers;
  }

  /**
   * Collect all the sheets and columns containing test data in the given
   * <file>. 
   * Used to create the correct number of test cases for a test flow.
   * 
   * @param file
   * @return
   */
  public LinkedList<String> getTestDataSetNames(File file)
  {
    LinkedList<String> testDataSets = new LinkedList<String>();
    Workbook workbook = null;
    
    try
    {
      LOGGER.trace("Get testdata sets for: {}", file);
      
      workbook = new XSSFWorkbook(file);
      workbookReader = new WorkbookReader(workbook);
      
      for(Sheet sheet : workbook)
      {
        LOGGER.trace("Sheet: {}", sheet.getSheetName());
        
        Iterator<Row> rowIterator = sheet.rowIterator();

        if (rowIterator.hasNext())
        {
          // read headers ("Keywords", DataSet1, ...)
          Map<Integer, String> headers = readHeaderRow(rowIterator.next());
          
          for(Map.Entry<Integer, String> entry : headers.entrySet())
          {
            if(!entry.getValue().toLowerCase().equals("keywords"))
            {
              LOGGER.debug("Add test data set '{}'", () -> sheet.getSheetName() + File.separator + entry.getValue());
              testDataSets.add(sheet.getSheetName() + File.separator + entry.getValue());
            }
          }
        }
      }
    }
    catch(Exception eX)
    {
      LOGGER.error("Exception occured while reading the sheet names from file '{}''", file, eX);
    }
    finally
    {
      try
      {
        workbook.close();
      }
      catch (IOException eX)
      {
        LOGGER.warn(eX);
      }
    }
    return testDataSets;
  }

  
  /**
   * Temporary function until class is rewritten!
   * Convert the requested map to a set of properties.
   * 
   * @param testData
   * @param keySet
   * @return
   */
  public Properties getAsProperties(LinkedHashMap<String, Map<String, String>> testData, String keySet)
  {
    Properties properties = new Properties();
    
    try
    {
      LOGGER.trace("Convert test data set '{}' to property", keySet);
      
      for(String key : testData.keySet())
      {
        LOGGER.trace("Data set: '{}' (Hit:{})", key, key.equals(keySet));
        
        if(key.equals(keySet))
        {  
          Map<String,String> map = testData.get(key);
          
          for(Entry<String,String> entry : map.entrySet())
          {
            if(cellReader.isKey(entry.getValue())){
              String value = entry.getValue().substring(1, entry.getValue().length() - 1);
                if(testRunner == null){
                    LOGGER.error("testRunner is null, property " + entry.getValue() + " not stored.");
                    break;
                }
                LOGGER.trace("Property value is a property, retrieving value from collection.");
                if(testRunner.hasPropertyValue(value))
                {
                  LOGGER.trace("> {}: '{}'", entry.getKey(), testRunner.getPropertyValue(value));
                  properties.setProperty(entry.getKey(), testRunner.getPropertyValue(value));
                }
                else
                {
                  throw new Exception("No property value found for property " + entry.getValue() + " and key " + entry.getKey());
                }
            } else {
            LOGGER.trace("> {}: '{}'", entry.getKey(), entry.getValue());
            properties.setProperty(entry.getKey(), entry.getValue());
            }
          }
          break;
        }
      }
    }
    catch(Exception eX)
    {
      LOGGER.error("Error while converting Map '{}' to properties", keySet, eX);
    }
    
    return properties;
  }

}


