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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Hielke de Haan
 * @since 0.0.1
 */
public class TestDataBuilder
{
  private final static Logger LOGGER = LogManager.getLogger();
  private WorkbookReader workbookReader;

  LinkedHashMap<String, Map<String, String>> buildTestData(String path) throws Exception
  {
    Workbook workbook = new XSSFWorkbook(new File(path));
    workbookReader = new WorkbookReader(workbook);
    LinkedHashMap<String, Map<String, String>> testData = new LinkedHashMap<>();

    for (Sheet sheet : workbook)
    {
      LOGGER.trace("Sheet: {}", sheet::getSheetName);
      readSheet(sheet).forEach(testData::put);
    }

    return testData;
  }

  private LinkedHashMap<String, Map<String, String>> readSheet(Sheet sheet) throws Exception
  {
    LinkedHashMap<String, Map<String, String>> testData = new LinkedHashMap<>();
    Iterator<Row> rowIterator = sheet.rowIterator();

    if (rowIterator.hasNext())
    {
      // read headers
      Map<Integer, String> headers = readHeaderRow(rowIterator.next());

      // initialize submaps in testData map
      for (int colNum = 1; colNum < headers.size(); colNum++)
        testData.put(headers.get(colNum), new HashMap<>());

      while (rowIterator.hasNext())
      {
        Row row = rowIterator.next();

        // column 0 contains key
        Cell keyCell = row.getCell(0);
        if (keyCell != null)
        {
          String key = workbookReader.evaluateCellContent(keyCell);
          if (!key.isEmpty())
          {
            // columns 1 and on contain values
            for (int colNum = 1; colNum < headers.size(); colNum++)
            {
              Cell cell = row.getCell(colNum);
              if (cell != null)
              {
                String value = workbookReader.evaluateCellContent(cell);
                if (!value.isEmpty())
                {
                  LOGGER.debug("Found value '{}' for key '{}' in column '{}'", value, key, headers.get(colNum));
                  testData.get(headers.get(colNum)).put(key, value);
                } else
                {
                  LOGGER.debug("No value found for key '{}' in column '{}'; copying from column '{}'", key, headers.get(colNum), headers.get(colNum - 1));
                  testData.get(headers.get(colNum)).put(key, testData.get(headers.get(colNum - 1)).get(key));
                }
              }
            }
          } else
          {
            LOGGER.debug("Row {} is empty; skipping row", row.getRowNum());
          }
        } else
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
}
