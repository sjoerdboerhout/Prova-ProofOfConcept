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
import java.util.Map.Entry;
import java.util.regex.Pattern;

/**
 * @author Hielke de Haan
 * @since 0.0.1
 */
public class TestCaseBuilder
{
  private final static Logger LOGGER = LogManager.getLogger();
  private String flowWorkbookPath, 
                 dataWorkbookPath,
                 dataSetName,
                 testRootPath;
  private Workbook workbook;
  private WorkbookReader flowWorkbookReader;
  private WebActionFactory webActionFactory;
  private TestRunner testRunner;
  private Properties testDataKeywords;

  /**
   * Constructor with test root path and link to test runner 
   * 
   * @param testRootPath
   * @param testRunner
   * @throws IOException
   */
  public TestCaseBuilder(String testRootPath, TestRunner testRunner) throws IOException
  {
    LOGGER.debug("TestCaseBuilder: Path: {}", testRootPath);
    
    this.testRootPath = testRootPath;
    this.testRunner = testRunner;
    this.webActionFactory = new WebActionFactory();
    this.testDataKeywords = new Properties();
  }

  
  /**
   * Load the full test case. File name is part of the tc id.
   * 
   * @param testCase
   * @return
   * @throws Exception
   */
  public TestCase buildTestCase(TestCase testCase) throws Exception
  {
    //TestDataBuilder testdataBuilder;
    
    flowWorkbookPath = getFlowPathFromTCID(testCase.getId());
    dataWorkbookPath = getDataPathFromTCID(testCase.getId());
    dataSetName      = getKeywordSetFromTCID(testCase.getId());
    
    
    LOGGER.debug("Load data file: '{}'", dataWorkbookPath);
    if(new File(dataWorkbookPath).isFile())
    {
      testDataKeywords.putAll(new TestDataBuilder().buildTestData(dataWorkbookPath, dataSetName));
      
      if(LOGGER.isTraceEnabled())
        printTestDataKeywords(testDataKeywords);
    }
    else
    {
      throw new Exception(dataWorkbookPath + " is not a valid file!");
    }
    
    
    LOGGER.debug("Load flow data file: '{}'", flowWorkbookPath);
    if(new File(flowWorkbookPath).isFile())
    {
      workbook = new XSSFWorkbook(flowWorkbookPath);
      flowWorkbookReader = new WorkbookReader(workbook);
  
      for (Sheet sheet : workbook)
      {
        LOGGER.trace("Sheet: {}", sheet::getSheetName);
        if (new SheetPrefixValidator(sheet).validate())
        {
          parseSheet(testCase, sheet);
        }
      }
    }
    else
    {
      throw new Exception(flowWorkbookPath + " is not a valid file!");
    }
    
    return testCase;
  }

  /**
   * Parse the given sheet. Find tags and execute related actions to
   * retrieve/set the data for this test case.
   * 
   * @param testCase
   * @param sheet
   * @throws Exception
   */
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
          String firstCellContent = flowWorkbookReader.evaluateCellContent(firstCell);
          if (flowWorkbookReader.isTag(firstCellContent))
          {
            String tagName = flowWorkbookReader.getTagName(firstCellContent);
            LOGGER.trace("Found tag: {}", tagName);
            switch (tagName)
            {
              case "tcid":
                // Already read in earlier state
                break;
              case "beschrijving":
                testCase.setSummary(flowWorkbookReader.readProperty(row, firstCell));
                break;
              case "functionaliteit":
                // TODO add field to TestCase
                break;
              case "issueid":
                testCase.setIssueId(flowWorkbookReader.readProperty(row, firstCell));
                break;
              case "prioriteit":
                testCase.setPriority(flowWorkbookReader.readProperty(row, firstCell));
                break;
              case "project":
                testCase.setProjectName(flowWorkbookReader.readProperty(row, firstCell));
                break;
              case "requirement":
                // TODO add field to TestCase
                break;
              case "status":
                testCase.setStatus(TestStatus.valueOf(flowWorkbookReader.readProperty(row, firstCell)));
                break;
              case "labels":
                // Ignore
                break;
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

  /**
   * Parse test actions and add the actions found to the correct action list.
   * (Setup, action or teardown)
   * 
   * @param testCase
   * @param sheet
   * @param rowNum
   * @param tagName
   * @throws Exception
   */
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

  
  /**
   * Read test action from a referenced sheet in another file
   * 
   * @param rowMap
   * @return
   * @throws Exception
   */
  private List<TestAction> readTestActionsFromReference(Map<String, String> rowMap) throws Exception
  {
    Sheet nextSheet;
    
    if (rowMap.get("package").isEmpty())
    {
      LOGGER.debug("Find sheet {} in same workbook", rowMap.get("test"));
      nextSheet = workbook.getSheet(rowMap.get("test"));

      if (nextSheet == null)
        throw new Exception("Sheet " + rowMap.get("test") + " not found in workbook " + flowWorkbookPath);
    } 
    else
    {
      LOGGER.debug("Find sheet {} in package {}", rowMap.get("test"), rowMap.get("package"));
      String nextPath = getWorkbookFromPackage(rowMap.get("package"));
      
      if (!new File(nextPath).exists())
        throw new Exception("Workbook '" + nextPath + "' not found");

      nextSheet = new XSSFWorkbook(nextPath).getSheet(rowMap.get("test"));

      if (nextSheet == null)
        throw new Exception("Sheet " + rowMap.get("test") + " not found in workbook " + nextPath);
    }
    
    // TODO READ keywords for reference sheet!
    
    return readTestActionsFromSheet(nextSheet);
  }

  
  /**
   * Scan an imported sheet for test actions
   * 
   * @param sheet
   * @return
   * @throws Exception
   */
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
          String firstCellContent = flowWorkbookReader.evaluateCellContent(firstCell);
          
          if (flowWorkbookReader.isTag(firstCellContent))
          {
            // get tag
            String tagName = flowWorkbookReader.getTagName(firstCellContent);
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

  /**
   * Scan all rows on the given <sheet> and parse all actions and 
   * import referenced sheets.
   * 
   * @param testActions
   * @param sheet
   * @param rowNum
   * @param tagName
   * @throws Exception
   */
  private void parseTestActionSection(List<TestAction> testActions, Sheet sheet, MutableInt rowNum, String tagName) throws Exception
  {
    // get header row
    Map<Integer, String> headers = readSectionHeaderRow(sheet, rowNum);
    Map<String, String> rowMap;
    String keyword;
    
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
              keyword = rowMap.get(key);
              
              if( keyword.length() > 2 &&
                  keyword.startsWith("{") &&
                  keyword.endsWith("}")
                )
              {
                // Remove the { } around the keyword
                keyword = keyword.trim().substring(1, keyword.length()-1);
                
                if(testDataKeywords.containsKey(keyword))
                {
                  LOGGER.trace("Substitue key '{}'. Keyword '{}' with value '{}'", key, keyword, testDataKeywords.getProperty(keyword));
                  keyword = testDataKeywords.getProperty(keyword);
                }
                else
                {
                  throw new Exception("Keyword '" + keyword + "' in sheet '" + sheet.getSheetName() + "' not defined with a value.");
                }
              }
                
              testAction.setAttribute(key, keyword);
              LOGGER.trace("Read '{}' action attribute '{}' = '{}'", rowMap.get("actie").toUpperCase(), key, keyword);
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

  /**
   * Parse an test action header and get all variable names for the test actions
   * @param sheet
   * @param rowNum
   * @return
   * @throws Exception
   */
  private Map<Integer, String> readSectionHeaderRow(Sheet sheet, MutableInt rowNum) throws Exception
  {
    Map<Integer, String> headers = new HashMap<>();
    String header;

    rowNum.increment();
    Row headerRow = sheet.getRow(rowNum.intValue());

    for (Cell headerCell : headerRow)
    {
      header = flowWorkbookReader.evaluateCellContent(headerCell);
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

  
  /**
   * Read the given <rowNum> and save the information on the row
   * 
   * @param sheet
   * @param rowNum
   * @param headers
   * @return
   * @throws Exception
   */
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
        rowMap.put(headerEntry.getValue(), flowWorkbookReader.evaluateCellContent(labelCell));
      else
        rowMap.put(headerEntry.getValue(), null);
    }

    // break if row map only contains nulls
    if (!CollectionUtils.exists(rowMap.values(), NotNullPredicate.INSTANCE))
      return null;

    return rowMap;
  }

  
  /**
   * Extract the path of the workbook containing the test flow from TCID
   * 
   * @param tcid
   * @return
   */
  private String getFlowPathFromTCID(String tcid)
  {
    // Split the flow and data file (if exists in tcid)
    tcid = tcid.substring(0, tcid.lastIndexOf(File.separator + File.separator));
    
    // Strip the sheet name at the end of the TCID
    LOGGER.trace("Flow path from TCID: '{}'", tcid.substring(0, tcid.lastIndexOf(File.separator)));
    return tcid.substring(0, tcid.lastIndexOf(File.separator));
  }

  
  /**
   * Extract the path of the workbook containing the test data from TCID
   * 
   * @param tcid
   * @return
   */
  private String getDataPathFromTCID(String tcid) throws Exception
  {
    String path = getFlowPathFromTCID(tcid);
    String separator = File.separator + File.separator;

    path = path.substring(0,  path.lastIndexOf(File.separator));
    
    path += File.separator +
            this.testRunner.getPropertyValue(Config.PROVA_TESTS_DATA_DIR) +
            File.separator +
            tcid.substring(tcid.lastIndexOf(separator) + separator.length(), 
                          tcid.lastIndexOf(".xlsx") + ".xlsx".length());
    
    LOGGER.trace("Data path from TCID: '{}'", path);
        
    return path;
  }
  
  
  /**
   * Extract the name of the keyword set containing the test data from TCID
   * 
   * @param tcid
   * @return
   */
  private String getKeywordSetFromTCID(String tcid) throws Exception
  {
    LOGGER.trace("Get Keyword Set From TCID: '{}'", tcid);
    
    tcid = tcid.substring(tcid.lastIndexOf(File.separator) + File.separator.length(), tcid.length());

    LOGGER.trace("Found keyword set from TCID: '{}'", tcid);
    
    return tcid;
  }
  
  
  /**
   * Convert a package name to a correct filename
   * 
   * @param _package
   * @return
   */
  private String getWorkbookFromPackage(String _package)
  {
    LOGGER.trace("getWorkbookFromPackage: '{}'", _package);
    
    return testRootPath + File.separator + _package.replace(".", File.separator) + ".xlsm";
  }
  

  /**
   * For trace logging, print the data in given test map.
   * 
   * @param testData
   */
  @SuppressWarnings("unused")
  private void printTestDataMap(LinkedHashMap<String, Map<String, String>> testData)
  {
    LOGGER.trace("Print TestData Map with {} sets data:", testData.size());
    
    for(String key : testData.keySet())
    {
      LOGGER.trace("Data set: '{}'", key);
      
      Map<String,String> map = testData.get(key);
      
      for(Entry<String,String> entry : map.entrySet())
      {
        LOGGER.trace("> {}: '{}'", entry.getKey(), entry.getValue());
      }
    }
  }
  
  
  /**
   * For trace logging, log all items in the property set
   * 
   * @param props
   */
  private void printTestDataKeywords(Properties props)
  {
    LOGGER.trace("Print TestData Properties with {} sets data:", props.size());
    
    for(String key : props.stringPropertyNames())
    {
      LOGGER.trace("> " + key + " => " + props.getProperty(key));
    }
  }
}
