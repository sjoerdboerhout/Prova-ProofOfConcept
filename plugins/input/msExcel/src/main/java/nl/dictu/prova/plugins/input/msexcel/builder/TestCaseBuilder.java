package nl.dictu.prova.plugins.input.msexcel.builder;

import nl.dictu.prova.Config;
import nl.dictu.prova.TestRunner;
import nl.dictu.prova.framework.TestAction;
import nl.dictu.prova.framework.TestCase;
import nl.dictu.prova.framework.TestStatus;
import nl.dictu.prova.framework.web.WebActionFactory;
import nl.dictu.prova.plugins.input.msexcel.reader.WorkbookReader;
import nl.dictu.prova.plugins.input.msexcel.validator.SheetPrefixValidator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.functors.NotNullPredicate;
import org.apache.commons.lang.mutable.MutableInt;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author Hielke de Haan
 * @since 0.0.1
 */
public class TestCaseBuilder
{
  private final static Logger LOGGER = LogManager.getLogger();
  private String workbookPath, testRootPath;
  private Workbook workbook;
  private WorkbookReader workbookReader;
  private WebActionFactory webActionFactory;
  private TestRunner testRunner;

  public TestCaseBuilder(String testRootPath, TestRunner testRunner) throws IOException
  {
    this.testRootPath = testRootPath;
    this.testRunner = testRunner;
    this.webActionFactory = new WebActionFactory();
  }

  public TestCase buildTestCase(TestCase testCase) throws Exception
  {
    workbookPath = getPathFromTCID(testCase.getId());
    LOGGER.trace("Path: {}", workbookPath);
    workbook = new XSSFWorkbook(workbookPath);
    workbookReader = new WorkbookReader(workbook);

    for (Sheet sheet : workbook)
    {
      LOGGER.trace("Sheet: {}", sheet::getSheetName);
      if (new SheetPrefixValidator(sheet).validate())
        parseSheet(testCase, sheet);
    }
    return testCase;
  }

  private void parseSheet(TestCase testCase, Sheet sheet) throws Exception
  {
    MutableInt rowNum = new MutableInt(sheet.getFirstRowNum());
    while (rowNum.intValue() < sheet.getLastRowNum())
    {
      Row row = sheet.getRow(rowNum.intValue());
      if (row != null)
      {
        Cell firstCell = row.getCell(0);
        if (firstCell != null)
        {
          String firstCellContent = workbookReader.evaluateCellContent(firstCell);
          if (workbookReader.isTag(firstCellContent))
          {
            String tagName = workbookReader.getTagName(firstCellContent);
            LOGGER.trace("Found tag: {}", tagName);
            switch (tagName)
            {
              case "beschrijving":
                // TODO add field to TestCase
                break;
              case "functionaliteit":
                // TODO add field to TestCase
                break;
              case "issueid":
                testCase.setIssueId(workbookReader.readProperty(row, firstCell));
                break;
              case "prioriteit":
                testCase.setPriority(workbookReader.readProperty(row, firstCell));
                break;
              case "project":
                testCase.setProjectName(workbookReader.readProperty(row, firstCell));
                break;
              case "requirement":
                // TODO add field to TestCase
                break;
              case "status":
                testCase.setStatus(TestStatus.valueOf(workbookReader.readProperty(row, firstCell)));
                break;
              case "labels":
              case "setup":
              case "test":
              case "teardown":
                parseTestCaseSection(testCase, sheet, rowNum, tagName);
                break;
              default:
                LOGGER.warn("Ignoring unknown tag {}", tagName);
            }
          }
        }
      }
      rowNum.increment();
    }
  }

  private void parseTestCaseSection(TestCase testCase, Sheet sheet, MutableInt rowNum, String tagName) throws Exception
  {
    Map<Integer, String> headers = readSectionHeaderRow(sheet, rowNum);
    Map<String, String> rowMap;

    while ((rowMap = readRow(sheet, rowNum, headers)) != null)
    {
      switch (tagName)
      {
        case "labels":
          // TODO process label
          break;
        case "setup":
          readTestActionsFromReference(rowMap).forEach(testCase::addSetUpAction);
          break;
        case "test":
          readTestActionsFromReference(rowMap).forEach(testCase::addTestAction);
          break;
        case "teardown":
          readTestActionsFromReference(rowMap).forEach(testCase::addTearDownAction);
          break;
      }
    }
  }

  private List<TestAction> readTestActionsFromReference(Map<String, String> rowMap) throws Exception
  {
    Sheet nextSheet;
    
    if (rowMap.get("package").isEmpty())
    {
      LOGGER.debug("Find sheet {} in same workbook", rowMap.get("test"));
      nextSheet = workbook.getSheet(rowMap.get("test"));

      if (nextSheet == null)
        throw new Exception("Sheet " + rowMap.get("test") + " not found in workbook " + workbookPath);
    } 
    else
    {
      LOGGER.debug("Find sheet {} in package {}", rowMap.get("test"), rowMap.get("package"));
      String nextPath = getWorkbookFromPackage(rowMap.get("package"));

      if (!new File(nextPath).exists())
        throw new Exception("Workbook " + nextPath + " not found");

      nextSheet = new XSSFWorkbook(nextPath).getSheet(rowMap.get("test"));

      if (nextSheet == null)
        throw new Exception("Sheet " + rowMap.get("test") + " not found in workbook " + nextPath);
    }
    return readTestActionsFromSheet(nextSheet);
  }

  private List<TestAction> readTestActionsFromSheet(Sheet sheet) throws Exception
  {
    List<TestAction> testActions = new ArrayList<>();
    MutableInt rowNum = new MutableInt(sheet.getFirstRowNum());
    
    while (rowNum.intValue() < sheet.getLastRowNum())
    {
      Row row = sheet.getRow(rowNum.intValue());
      
      if (row != null)
      {
        Cell firstCell = row.getCell(0);
        if (firstCell != null)
        {
          String firstCellContent = workbookReader.evaluateCellContent(firstCell);
          
          if (workbookReader.isTag(firstCellContent))
          {
            // get tag
            String tagName = workbookReader.getTagName(firstCellContent);
            LOGGER.trace("Found tag: {}", tagName);
            
            switch (tagName)
            {
              case "sectie":
              case "tc":
                parseTestActionSection(testActions, sheet, rowNum, tagName);
            }
          }
        }
      }
      rowNum.increment();
    }
    return testActions;
  }

  private void parseTestActionSection(List<TestAction> testActions, Sheet sheet, MutableInt rowNum, String tagName) throws Exception
  {
    // get header row
    Map<Integer, String> headers = readSectionHeaderRow(sheet, rowNum);
    Map<String, String> rowMap;
    while ((rowMap = readRow(sheet, rowNum, headers)) != null)
    {
      switch (tagName)
      {
        case "sectie":
          TestAction testAction = webActionFactory.getAction(rowMap.get("actie"));
          String locatorName = rowMap.get("locator").toLowerCase();
          
          LOGGER.debug("Action: '{}', Locator: '{}' (xpath: {})", 
                        rowMap.get("actie").toUpperCase(), 
                        locatorName,
                        testRunner.getPropertyValue(Config.PROVA_PLUGINS_OUT_WEB_LOCATOR_PFX + "." + locatorName));
          
          testAction.setTestRunner(testRunner);
          testAction.setAttribute("xpath", testRunner.getPropertyValue(Config.PROVA_PLUGINS_OUT_WEB_LOCATOR_PFX + "." + locatorName));
          
          for (String key : rowMap.keySet())
          {
            if (!key.equals("actie"))
            {
              // TODO 
              testAction.setAttribute(key, rowMap.get(key));
              // TODO substitute parameters
              LOGGER.trace("Read '{}' action attribute '{}' = '{}'", rowMap.get("actie").toUpperCase(), key, rowMap.get(key));
            }
          }
          LOGGER.trace("Read '{}' action '{}'", rowMap.get("actie"), testAction);
          testActions.add(testAction);
          break;
        case "tc":
          readTestActionsFromReference(rowMap).forEach(testActions::add);
          break;
      }
    }
  }

  private Map<Integer, String> readSectionHeaderRow(Sheet sheet, MutableInt rowNum) throws Exception
  {
    Map<Integer, String> headers = new HashMap<>();
    String header;

    rowNum.increment();
    Row headerRow = sheet.getRow(rowNum.intValue());

    for (Cell headerCell : headerRow)
    {
      header = workbookReader.evaluateCellContent(headerCell);
      header = header.replace("*", "");
      header = header.replace(":", "");
      header = header.toLowerCase();
      header = header.trim();

      if (!header.isEmpty())
        headers.put(headerCell.getColumnIndex(), header);
    }

    LOGGER.trace("Read section headers {}", headers);
    return headers;
  }

  private Map<String, String> readRow(Sheet sheet, MutableInt rowNum, Map<Integer, String> headers) throws Exception
  {
    rowNum.increment();
    Row labelRow = sheet.getRow(rowNum.intValue());

    // break if row is empty
    if (labelRow == null)
      return null;

    Map<String, String> rowMap = new HashMap<>();
    Set<Map.Entry<Integer, String>> headerEntries = headers.entrySet();
    for (Map.Entry<Integer, String> headerEntry : headerEntries)
    {
      Cell labelCell = labelRow.getCell(headerEntry.getKey());
      if (labelCell != null)
        rowMap.put(headerEntry.getValue(), workbookReader.evaluateCellContent(labelCell));
      else
        rowMap.put(headerEntry.getValue(), null);
    }

    // break if row map only contains nulls
    if (!CollectionUtils.exists(rowMap.values(), NotNullPredicate.INSTANCE))
      return null;

    return rowMap;
  }

  private String getPathFromTCID(String tcid)
  {
    return tcid.substring(0, tcid.lastIndexOf(File.separator));
  }

  private String getWorkbookFromPackage(String _package)
  {
    return testRootPath + File.separator + _package.replace(".", File.separator) + ".xlsm";
  }
}
