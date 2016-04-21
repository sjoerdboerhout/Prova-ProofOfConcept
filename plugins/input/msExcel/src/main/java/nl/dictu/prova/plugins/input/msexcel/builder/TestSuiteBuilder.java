package nl.dictu.prova.plugins.input.msexcel.builder;

import nl.dictu.prova.framework.TestCase;
import nl.dictu.prova.framework.TestSuite;
import nl.dictu.prova.plugins.input.msexcel.reader.WorkbookReader;
import nl.dictu.prova.plugins.input.msexcel.validator.SheetPrefixValidator;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;

/**
 * @author Hielke de Haan
 * @since 0.0.1
 */
public class TestSuiteBuilder
{
  private static final Logger LOGGER = LogManager.getLogger();
  private static final FileFilter directoryFilter = file -> file.isDirectory() && !file.getName().equals("testdata");
  private static final FileFilter excelFilter = file -> !file.isDirectory() && !file.getName().startsWith("~") && Arrays.asList("xlsm", "xlsx").contains(FilenameUtils.getExtension(file.getName()));

  TestSuite buildTestSuite(File rootDirectory) throws Exception
  {
    LOGGER.trace("Directory: {}", rootDirectory::getPath);
    TestSuite testSuite = new TestSuite(rootDirectory.getPath());
    testSuite = addTestCases(testSuite);

    File[] directories = rootDirectory.listFiles(directoryFilter);
    for (File directory : directories)
    {
      testSuite.addTestSuite(buildTestSuite(directory));
    }

    return testSuite;
  }

  private TestSuite addTestCases(TestSuite testSuite) throws Exception
  {
    File[] excelFiles = new File(testSuite.getId()).listFiles(excelFilter);

    for (File excelFile : excelFiles)
    {
      LOGGER.trace("File: {}", excelFile);
      Workbook workbook = new XSSFWorkbook(excelFile);
      WorkbookReader workbookReader = new WorkbookReader(workbook);

      for (Sheet sheet : workbook)
      {
        LOGGER.trace("Sheet: {}", sheet::getSheetName);
        if (new SheetPrefixValidator(sheet).validate())
        {
          for (Row row : sheet)
          {
            if (row != null)
            {
              Cell cell = row.getCell(0);
              if (cell != null)
              {
                String cellContent = workbookReader.evaluateCellContent(cell);
                if (workbookReader.isTag(cellContent))
                {
                  // examine the first tag that is encountered and check if it is a [TCID] tag
                  // if it is, add a new test case to the test suite
                  // if it isn't, ignore the sheet
                  String tagName = workbookReader.getTagName(cellContent);
                  LOGGER.debug("Found tag: {}", tagName);
                  if ("tcid".equals(tagName))
                  {
                    testSuite.addTestCase(new TestCase(excelFile.getPath() + File.separator + workbookReader.readProperty(row, cell)));
                  }
                  break; // exit for
                }
              }
            }
          }
        }
      }
    }
    return testSuite;
  }


}
