package nl.dictu.prova.plugins.input.msexcel.builder;

import nl.dictu.prova.plugins.input.msexcel.reader.WorkbookReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

/**
 * @author Hielke de Haan
 * @since 0.0.1
 */
public class TestDataBuilder
{
  private final static Logger LOGGER = LogManager.getLogger();
  private WorkbookReader workbookReader;
  
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
      readSheet(sheet).forEach(testData::put);
    }

    LOGGER.trace("Number of found testData sets: {}", testData.size());
    
    return getAsProperties(testData, keySet);
  }

  /**
   * 
   * @param sheet
   * @return
   * @throws Exception
   */
  private LinkedHashMap<String, Map<String, String>> readSheet(Sheet sheet) throws Exception
  {
    LinkedHashMap<String, Map<String, String>> testData = new LinkedHashMap<>();
    Iterator<Row> rowIterator = sheet.rowIterator();
    
    LOGGER.trace("TestData Builder readSheet: '{}'", sheet.getSheetName());
    
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
          String key = workbookReader.evaluateCellContent(keyCell);
          LOGGER.trace("Found key: '{}'", key);
          
          if (!key.isEmpty())
          {
            // columns 1 and on contain values
            for (int colNum = 1; colNum < headers.size(); colNum++)
            {
              Cell cell = row.getCell(colNum);
              if (cell != null)
              {
                String value = workbookReader.evaluateCellContent(cell);
                
                // Empty values are only allowed in column 1
                // or keep all columns empty.
                if(value.isEmpty() && (colNum > 1))
                {
                  LOGGER.debug("No value found for key '{}' in column '{}'; copying from column '{}' ({})", key, headers.get(colNum), headers.get(colNum - 1), colNum);
                  testData.get(headers.get(colNum)).put(key, testData.get(headers.get(colNum - 1)).get(key));
                }
                else
                {
                  LOGGER.debug("Found value '{}' for key '{}' in column '{}'", value, key, headers.get(colNum));
                  testData.get(headers.get(colNum)).put(key, value);
                }
              }
            }
          } 
          else
          {
            LOGGER.debug("Row {} is empty; skipping row", row.getRowNum());
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
      String cellContent = workbookReader.evaluateCellContent(cell);
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
              LOGGER.trace("Add test data set '{}'", () -> sheet.getSheetName() + File.separator + entry.getValue());
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
            LOGGER.trace("> {}: '{}'", entry.getKey(), entry.getValue());
            properties.setProperty(entry.getKey(), entry.getValue());
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


