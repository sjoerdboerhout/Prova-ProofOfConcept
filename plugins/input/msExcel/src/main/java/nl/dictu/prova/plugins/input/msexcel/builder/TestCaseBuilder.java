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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import nl.dictu.prova.framework.db.DbActionFactory;
import nl.dictu.prova.plugins.input.msexcel.reader.CellReader;

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
                 testRootPath,
                 dbQuery,
                 soapMessage;
  private Workbook workbook;
  private WorkbookReader flowWorkbookReader;
  private WebActionFactory webActionFactory;
  private DbActionFactory dbActionFactory;
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
    LOGGER.trace("TestCaseBuilder: Path: {}", testRootPath);
    
    this.testRootPath = testRootPath;
    this.testRunner = testRunner;
    this.webActionFactory = new WebActionFactory();
    this.dbActionFactory = new DbActionFactory();
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
    LOGGER.trace("START BUILDING A TESTCASE FOR '{}'", testCase.getId() );
    
    flowWorkbookPath = getFlowPathFromTCID(testCase.getId());
    dataWorkbookPath = getDataPathFromTCID(testCase.getId());
    dataSetName      = getKeywordSetFromTCID(testCase.getId());
    
    if(dataWorkbookPath.length() > 0)
    {
      LOGGER.debug("Try to load data file: '{}'", dataWorkbookPath);
      
      if(new File(dataWorkbookPath).isFile())
      {
        testDataKeywords.putAll(new TestDataBuilder(testRunner).buildTestData(dataWorkbookPath, dataSetName));
        
        if(LOGGER.isTraceEnabled())
          printTestDataKeywords(testDataKeywords);
      }
      else
      {
        throw new Exception(dataWorkbookPath + " is not a valid file!");
      } 
    }
    else
      LOGGER.debug("No data file found for tc: '{}'", testCase.getId());
        
    
    LOGGER.debug("Load flow data file: '{}'", flowWorkbookPath);
    if(new File(flowWorkbookPath).isFile())
    {
      workbook = new XSSFWorkbook(flowWorkbookPath);
      flowWorkbookReader = new WorkbookReader(workbook);
  
      for (Sheet sheet : workbook)
      {
        LOGGER.trace("Sheet: {}", sheet::getSheetName);
        if (new SheetPrefixValidator(sheet).validate())
          parseSheet(testCase, sheet);        
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
                  LOGGER.warn("Ignoring unknown tag {} ({})", tagName, testCase.getId());
              
            }
          }
        }
      }
      rowNum.increment();
    }
  }
  
  /**
   * Parse the given sheet. Find tags and execute related actions to
   * retrieve/set the data for this test case.
   * 
   * @param sheet
   * @param rowNum
   * @param tagname
   * @throws Exception
   */
  private List<TestAction> parseDbTemplate(Sheet sheet, MutableInt rowNum, String tagName, List<Properties> dataset) throws Exception
  {     
    Map<Integer, String> headers = null;
    Map<String, String> rowMap = null;
    TestAction testAction = null;
    List<TestAction> testActions = new ArrayList<>();
    CellReader cellReader = new CellReader();
    
    LOGGER.info("Parsing Db template with sheet " + sheet.getSheetName());
    
    //Add input properties to the central properties collection
    if(dataset != null & !dataset.isEmpty()){
        for(Entry entry : dataset.get(0).entrySet()){
            testRunner.setPropertyValue((String) entry.getKey(), (String) entry.getValue());
        }
    }
    
    //Read and process the specified part of the SOAP template based on the tagname. Each tagname 
    //is linked to a unique TestAction. Together they form the basis for sending and testing.
    if (tagName.toLowerCase().equals("execute")){
        TestAction execute = DbActionFactory.getAction("PROCESSDBRESPONSE");
        execute.setTestRunner(testRunner);
        testActions.add(execute);
        
        //After adding execute TestAction, add tests from datasheet if available.
        if(dataset != null & !dataset.isEmpty()){
            for(Entry entry : dataset.get(1).entrySet()){
                try{
                    TestAction test = dbActionFactory.getAction("EXECUTEDBTEST");
                    test.setAttribute((String) entry.getKey(), (String) entry.getValue());
                    test.setTestRunner(testRunner);
                    testActions.add(test);
                } catch (Exception eX){
                    LOGGER.error("Exception while setting attribute!" + eX.getMessage());
                    eX.printStackTrace();
                }
            }
        } 
    } else if(tagName.toLowerCase().equals("query")){
        testAction = DbActionFactory.getAction("SETQUERY");
        headers = readSectionHeaderRow(sheet, rowNum);
        dbQuery = "";
        
        while((rowMap = readRow(sheet, rowNum, headers))!= null)
        {
            if(rowMap.isEmpty()){
                LOGGER.debug("End of query/message block reached at row " + rowNum);
                break;
            }
            for(String entry : rowMap.values()){
                if(entry != null & entry.length() > 0){
                    String processedCellValue = replaceKeywords(entry);
                    if(processedCellValue.trim().equalsIgnoreCase("skipcell"))
                        continue;
                    dbQuery += processedCellValue + " ";
                }
            }
        }
        testAction.setAttribute("prova.properties.query", dbQuery);
        dbQuery = "";
    } else if (tagName.toLowerCase().equals("queryproperties")){
        testAction = DbActionFactory.getAction("SETDBPROPERTIES");
        headers = readSectionHeaderRow(sheet, rowNum);
        String prefix = null;
        
        while((rowMap = readRow(sheet, rowNum, headers))!= null)
        {
            //Check for prefix on current row. If found then check for existence in 
            //global properties with most recent incrementation (e.g. "SOAP_message10_")
            
            //PREFIX
            if(rowMap.containsKey("prefix")){
                LOGGER.trace("Prefix found. Processing.");
                prefix = rowMap.get("prefix");
                Boolean prefixFound = true;
                Integer counter = 1;
                
                while(prefixFound){
                    if(this.testRunner.hasPropertyValue("prova.properties.used." + prefix + counter)){
                        counter++;
                    } else {
                        LOGGER.trace("Prefix of type '" + prefix + "' has highest incrementation number: " + counter);
                        prefix = prefix + counter;
                        LOGGER.trace("Setting prefix to " + prefix + " on key prova.properties.prefix");
                        prefixFound = false;
                    }
                }
                //Adding prefix as key to properties so properties can be scanned for existence.
                this.testRunner.setPropertyValue("prova.properties.used." + prefix, prefix);
                //Setting prefix as current prefix on key prova.properties.prefix.
                testAction.setAttribute("prova.properties.prefix", prefix);
            } else {
                throw new Exception("No prefix has been supplied! Please add a 'Prefix' key and value below your [QueryProperties] tag.");
            }
            
            //PASSWORD
            if(rowMap.containsKey("password")){
                LOGGER.trace("Password found. Processing.");
                if(cellReader.isKey(rowMap.get("password"))){
                    LOGGER.trace("Password value is a key, retrieving property value.");
                    String password = this.testRunner.getPropertyValue(cellReader.getKeyName(rowMap.get("password")));
                    if(password != null){
                        testAction.setAttribute("prova.properties.password", password);
                    } else {
                        LOGGER.debug("Unable to process user tag");
                    }
                } else {
                    testAction.setAttribute("prova.properties.password", rowMap.get("password"));
                }
            }
            
            //USER
            if(rowMap.containsKey("user")){
                LOGGER.trace("User found. Processing.");
                if(cellReader.isKey(rowMap.get("user"))){
                    LOGGER.trace("User value is a key, retrieving property value.");
                    String user = this.testRunner.getPropertyValue(cellReader.getKeyName(rowMap.get("user")));
                    if(user != null){
                        testAction.setAttribute("prova.properties.user", user);
                    } else {
                        LOGGER.debug("Unable to process user tag");
                    }
                } else {
                    testAction.setAttribute("prova.properties.user", rowMap.get("user"));
                }
            }
            
            //ROLLBACK
            if(rowMap.containsKey("rollback")){
                LOGGER.trace("Rollback found. Processing.");
                String rollback = rowMap.get("rollback").trim().toLowerCase();
                if(!(rollback.equals("false") || rollback.equals("true"))){
                    throw new Exception("Rollback value must be 'false' or 'true'!");
                }
                testAction.setAttribute("prova.properties.rollback", rollback);
            } 
            
            //ADDRESS
            if(rowMap.containsKey("address")){
                LOGGER.trace("Address found. Processing.");
                if(cellReader.isKey(rowMap.get("address"))){
                    LOGGER.trace("Address value is a key, retrieving property value.");
                    String address = this.testRunner.getPropertyValue(cellReader.getKeyName(rowMap.get("address")));
                    if(address != null){
                        testAction.setAttribute("prova.properties.address", address);
                    } else {
                        LOGGER.debug("Unable to process address tag");
                    }
                } else {
                    testAction.setAttribute("prova.properties.address", rowMap.get("address"));
                }
            } else {
                throw new Exception("No address has been supplied! Please add a 'Address' key and value below your [QueryProperties] tag.");
            }
        }
    } 
    if(testAction != null){
        testAction.setTestRunner(testRunner);
        testActions.add(testAction);
    }
    return testActions;
  }

  /**
   * Parse the given sheet. Find tags and execute related actions to
   * retrieve/set the data for this test case.
   * 
   * @param sheet
   * @param rowNum
   * @param tagname
   * @throws Exception
   */
  private List<TestAction> parseSoapTemplate(Sheet sheet, MutableInt rowNum, String tagName, List<Properties> dataset) throws Exception
  {      
    Map<Integer, String> headers = null;
    Map<String, String> rowMap = null;
    TestAction testAction = null;
    List<TestAction> testActions = new ArrayList<>();
    CellReader cellReader = new CellReader();
    
    LOGGER.info("Parsing Soap template with sheet " + sheet.getSheetName());
    
    //Read and process the specified part of the SOAP template based on the tagname. Each tagname 
    //is linked to a unique TestAction. Together they form the basis for sending and testing.
    if (tagName.toLowerCase().equals("send")){
        testAction = DbActionFactory.getAction("PROCESSRESPONSE");
    } else if(tagName.toLowerCase().equals("message")){
        testAction = DbActionFactory.getAction("SETMESSAGE");
        headers = readSectionHeaderRow(sheet, rowNum);
        soapMessage = "";
        
        while((rowMap = readRow(sheet, rowNum, headers))!= null)
        {
            for(String entry : rowMap.values()){
                if(entry != null & entry.length() > 0){
                    soapMessage += entry;
                }
            }
            testAction.setAttribute("MESSAGE", soapMessage);
        }
        soapMessage = "";
    } else if (tagName.toLowerCase().equals("soapproperties")){
        testAction = DbActionFactory.getAction("SETPROPERTIES");
        headers = readSectionHeaderRow(sheet, rowNum);
        String prefix = null;
        
        while((rowMap = readRow(sheet, rowNum, headers))!= null)
        {
            //Check for prefix on current row. If found then check for existence in 
            //global properties with most recent incrementation (e.g. "SOAP_message10_")
            
            if(rowMap.containsKey("prefix")){
                LOGGER.trace("Prefix found. Processing.");
                prefix = rowMap.get("prefix");
                Boolean prefixFound = true;
                Integer counter = 1;
                
                while(prefixFound){
                    if(this.testRunner.hasPropertyValue("prova.properties.used." + prefix + counter)){
                        counter++;
                    } else {
                        LOGGER.trace("Prefix of type '" + prefix + "' has highest incrementation number: " + counter);
                        prefix = prefix + counter;
                        LOGGER.trace("Setting prefix to " + prefix + " on key prova.properties.prefix");
                        prefixFound = false;
                    }
                }
                //Adding prefix as key to properties so properties can be scanned for existence.
                this.testRunner.setPropertyValue("prova.properties.used." + prefix, prefix);
                //Setting prefix as current prefix on key prova.properties.prefix.
                testAction.setAttribute("prova.properties.prefix", prefix);
            } else {
                throw new Exception("No prefix has been supplied! Please add a 'Prefix' key and value below your [SoapProperties] tag.");
            }
            
            if(rowMap.containsKey("password")){
                LOGGER.trace("Password found. Processing.");
                if(cellReader.isKey(rowMap.get("password"))){
                    LOGGER.trace("Password value is a key, retrieving property value.");
                    String pass = this.testRunner.getPropertyValue(cellReader.getKeyName(rowMap.get("password")));
                    if(pass != null){
                        testAction.setAttribute("prova.properties.pass", pass);
                    } else {
                        LOGGER.debug("Unable to process user tag");
                    }
                } else {
                    testAction.setAttribute("prova.properties.pass", rowMap.get("pass"));
                }
            }
            
            if(rowMap.containsKey("user")){
                LOGGER.trace("User found. Processing.");
                if(cellReader.isKey(rowMap.get("user"))){
                    LOGGER.trace("User value is a key, retrieving property value.");
                    String user = this.testRunner.getPropertyValue(cellReader.getKeyName(rowMap.get("user")));
                    if(user != null){
                        testAction.setAttribute("prova.properties.user", user);
                    } else {
                        LOGGER.debug("Unable to process user tag");
                    }
                } else {
                    testAction.setAttribute("prova.properties.user", rowMap.get("user"));
                }
            }
            
            if(rowMap.containsKey("url")){
                LOGGER.trace("Url found. Processing.");
                if(cellReader.isKey(rowMap.get("url"))){
                    LOGGER.trace("Url value is a key, retrieving property value.");
                    String url = this.testRunner.getPropertyValue(cellReader.getKeyName(rowMap.get("url")));
                    if(url != null){
                        testAction.setAttribute("prova.properties.url", url);
                    } else {
                        LOGGER.debug("Unable to process url tag");
                    }
                } else {
                    testAction.setAttribute("prova.properties.url", rowMap.get("url"));
                }
            } else {
                throw new Exception("No url has been supplied! Please add a 'Url' key and value below your [SoapProperties] tag.");
            }
            
        }
    } 
    if(testAction == null){
        LOGGER.error("testAction has not been created, please check your tags");
    } else{
        testAction.setTestRunner(testRunner);
        testActions.add(testAction);
    }
    return testActions;
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
          readTestActionsFromReference(testCase, rowMap).forEach(testCase::addSetUpAction);
          break;
        case "test":
          readTestActionsFromReference(testCase, rowMap).forEach(testCase::addTestAction);
          break;
        case "teardown":
          readTestActionsFromReference(testCase, rowMap).forEach(testCase::addTearDownAction);
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
  private List<TestAction> readTestActionsFromReference(TestCase testCase, Map<String, String> rowMap) throws Exception
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
    
    //In case the sheet is of type 'SOAP' or 'DB', (optional) testdata is collected and used for iteration over the sheet.
    if(new SheetPrefixValidator(nextSheet).validate("SOAP") || new SheetPrefixValidator(nextSheet).validate("DB")){
        List<TestAction> testActions = new ArrayList<>();
        ArrayList<List<Properties>> testData = getSoapDbTestdata(testCase, nextSheet);
        if(testData.size() > 0){
            LOGGER.info(testData.size() + " sets of testdata found for sheet '" + nextSheet.getSheetName() + "'");
            for(List<Properties> dataSet : testData){
                testActions.addAll(readTestActionsFromSheet(testCase, nextSheet, dataSet));
            }
            return testActions;
        } else {
            LOGGER.info("No testdata found for sheet '" + nextSheet.getSheetName() + "', processing once.");
            testActions.addAll(readTestActionsFromSheet(testCase, nextSheet, null));
        }
    }
    
    // TODO READ keywords for reference sheet!
    
    return readTestActionsFromSheet(testCase, nextSheet, null);
  }

  
  /**
   * Scan an imported sheet for test actions
   * 
   * @param sheet
   * @return
   * @throws Exception
   */
  private List<TestAction> readTestActionsFromSheet(TestCase testCase, Sheet sheet, List<Properties> dataSet) throws Exception
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
                parseTestActionSection(testCase, testActions, sheet, rowNum, tagName);
                break;
              case "query":
              case "queryproperties":
              case "execute":
                parseDbTemplate(sheet, rowNum, tagName, dataSet).forEach(testCase::addTestAction);
                break;
              case "message":
              case "soapproperties":
              case "send":
                parseSoapTemplate(sheet, rowNum, tagName, dataSet).forEach(testCase::addTestAction);
                break;    
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
  private void parseTestActionSection(TestCase testCase, List<TestAction> testActions, Sheet sheet, MutableInt rowNum, String tagName) throws Exception
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
          TestAction testAction = WebActionFactory.getAction(rowMap.get("actie"));
          String locatorName = rowMap.get("locator").toLowerCase();
          String xPath = "";
          
          if( testRunner.hasPropertyValue(Config.PROVA_PLUGINS_OUT_DB_LOCATOR_PFX + "." + locatorName))
          {
            xPath = testRunner.getPropertyValue(Config.PROVA_PLUGINS_OUT_DB_LOCATOR_PFX + "." + locatorName);
            testAction.setAttribute("xpath", xPath);
          }
          
          LOGGER.trace("Action: '{}', Locator: '{}' (xpath: {})", 
                        rowMap.get("actie").toUpperCase(), 
                        locatorName,
                        xPath);
          
          testAction.setTestRunner(testRunner);
          testAction.setId(rowNum.toInteger());
          
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
                  LOGGER.trace("Substitute key '{}'. Keyword '{}' with value '{}'", key, keyword, testDataKeywords.getProperty(keyword));
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
          readTestActionsFromReference(testCase, rowMap).forEach(testActions::add);
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

    if(headerRow != null)
    {
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
    LOGGER.trace("Get flow path from TCID: '{}'", tcid);
    
    // Split the flow and data file (if exists in tcid)
    if(tcid.contains(File.separator + File.separator))
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
    LOGGER.trace("Get data path from TCID: '{}'", tcid);
    
    String path = getFlowPathFromTCID(tcid);
    String separator = File.separator + File.separator;

    path = path.substring(0, path.lastIndexOf(File.separator));
    
    // If tcid doesn't contain a data path then return with an empty string
    if(!tcid.contains(separator) || !tcid.contains(".xlsx"))
      return "";
    
    path += File.separator +
            this.testRunner.getPropertyValue(Config.PROVA_TESTS_DATA_DIR) +
            File.separator +
            tcid.substring(tcid.lastIndexOf(separator) + separator.length(), 
                          tcid.lastIndexOf(".xlsx") + ".xlsx".length());
    
    LOGGER.trace("Data path extracted from TCID: '{}'", path);
        
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
    
    String keyWordSet = "";
    
    if(tcid.contains("xlsx"))
      keyWordSet = tcid.substring(tcid.lastIndexOf(File.separator) + File.separator.length(), tcid.length());
          
    LOGGER.debug("Found keyword set from TCID: '{}' (tcid)", keyWordSet, tcid);
    
    return keyWordSet;
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

    private String replaceKeywords(String entry) throws Exception {
        Pattern pattern = Pattern.compile("\\{[A-Za-z0-9.]+\\}");
        Matcher matcher = pattern.matcher(entry);
        StringBuffer entryBuffer = new StringBuffer("");

        while(matcher.find()){
            String keyword = matcher.group(0).substring(1, matcher.group(0).length() - 1);
            if(keyword.equalsIgnoreCase("SKIPCELL")){
                LOGGER.debug("Skipping cell with keyword " + keyword);
                return "skipcell";
            }
            
            LOGGER.trace("Found keyword " + matcher.group(0) + " in supplied string.");
            if(!testRunner.hasPropertyValue(keyword))
                throw new Exception("No value found for property " + keyword);
            if(testRunner.getPropertyValue(keyword).equalsIgnoreCase("{SKIPCELL}")){
                LOGGER.debug("Skipping cell with keyword '{" + keyword + "}'");
                return "skipcell";
            }
            matcher.appendReplacement(entryBuffer, testRunner.getPropertyValue(keyword));
        }
        matcher.appendTail(entryBuffer);
        
        return entryBuffer.toString();
    }
    
    private ArrayList<List<Properties>> getSoapDbTestdata (TestCase testCase, Sheet sheet) throws Exception{
        ArrayList<List<Properties>> testData = new ArrayList<>();
        
        dataWorkbookPath = getFlowPathFromTCID(testCase.getId()).substring(0, getFlowPathFromTCID(testCase.getId()).lastIndexOf(File.separator));

        //Check if last character is a separator
        if((dataWorkbookPath.charAt(dataWorkbookPath.length() - 1) + "").equals(File.separator))        {
            dataWorkbookPath += "testdata" + File.separator + sheet.getSheetName() + ".xlsx";
        } else { 
            dataWorkbookPath += File.separator + "testdata" + File.separator + sheet.getSheetName() + ".xlsx";
        }

        //Load SOAP or DB specific testdata sheets
        if(new SheetPrefixValidator(sheet).validate("SOAP") || new SheetPrefixValidator(sheet).validate("DB")){
            if(new File(dataWorkbookPath).isFile()){
                LOGGER.debug("Reading SOAP/DB testdata sheet: '" + dataWorkbookPath + "'");
                testData = new TestDataBuilder(testRunner).buildTestDataAndTests(dataWorkbookPath, sheet.getSheetName());
                if(testData.isEmpty()){
                    LOGGER.debug("No testdata returned for path " + dataWorkbookPath + " and sheetname " + sheet.getSheetName());
                }
            } else {
                LOGGER.debug("No testdata file found for path " + dataWorkbookPath + " and sheetname " + sheet.getSheetName());
            }
        }
        return testData;
    }
}
