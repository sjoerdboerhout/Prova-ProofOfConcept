/**
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * <p>
 * http://ec.europa.eu/idabc/eupl
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * <p>
 * Date:      18-12-2016
 * Author(s): Sjoerd Boerhout, Robert Bralts & Coos van der Galiën
 * <p>
 */
package nl.dictu.prova.plugins.input.msexcel.builder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

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

import nl.dictu.prova.Config;
import nl.dictu.prova.TestRunner;
import nl.dictu.prova.framework.ActionFactory;
import nl.dictu.prova.framework.TestAction;
import nl.dictu.prova.framework.TestCase;
import nl.dictu.prova.framework.TestStatus;
import nl.dictu.prova.framework.db.DbActionFactory;
import nl.dictu.prova.framework.shell.ShellActionFactory;
import nl.dictu.prova.framework.soap.SoapActionFactory;
import nl.dictu.prova.framework.web.WebActionFactory;
import nl.dictu.prova.plugins.input.msexcel.reader.CellReader;
import nl.dictu.prova.plugins.input.msexcel.reader.WorkbookReader;
import nl.dictu.prova.plugins.input.msexcel.validator.SheetPrefixValidator;

/**
 * @author Hielke de Haan
 * @since 0.0.1
 */
public class TestCaseBuilder {

	private final static Logger LOGGER = LogManager.getLogger();
	private String flowWorkbookPath, dataWorkbookPath, dataSetName, testRootPath, messageOrQuery;
	private Workbook workbook;
	private WorkbookReader flowWorkbookReader;
	private ActionFactory actionFactory;
	private TestRunner testRunner;
	private Properties testDataKeywords;
	private String dateFormat = null;

	/**
	 * Constructor with test root path and link to test runner
	 *
	 * @param testRootPath
	 * @param testRunner
	 * @throws IOException
	 */
	public TestCaseBuilder(String testRootPath, TestRunner testRunner) throws IOException {
		LOGGER.trace("TestCaseBuilder: Path: {}", testRootPath);

		this.testRootPath = testRootPath;
		this.testRunner = testRunner;
		this.testDataKeywords = new Properties();

		try {
			// Argument for evaluateCellContent method
			this.dateFormat = testRunner.getPropertyValue(Config.PROVA_PLUGINS_INPUT_DATEFORMAT);
		} catch (Exception ex) {
		}
	}

	/**
	 * Load the full test case. File name is part of the tc id.
	 *
	 * @param testCase
	 * @return
	 * @throws Exception
	 */
	public TestCase buildTestCase(TestCase testCase) throws Exception {
		LOGGER.trace("START BUILDING A TESTCASE FOR '{}'", testCase.getId());

		flowWorkbookPath = getFlowPathFromTCID(testCase.getId());
		dataWorkbookPath = getDataPathFromTCID(testCase.getId());
		dataSetName = getKeywordSetFromTCID(testCase.getId());

		if (dataWorkbookPath.length() > 0) {
			LOGGER.debug("Try to load data file: '{}'", dataWorkbookPath);

			if (new File(dataWorkbookPath).isFile()) {
				testDataKeywords.putAll(new TestDataBuilder(testRunner).buildTestData(dataWorkbookPath, dataSetName));

				if (LOGGER.isTraceEnabled()) {
					printTestDataKeywords(testDataKeywords);
				}
			} else {
				throw new Exception(dataWorkbookPath + " is not a valid file!");
			}
		} else {
			LOGGER.debug("No data file found for tc: '{}'", testCase.getId());
		}

		LOGGER.debug("Load flow data file: '{}'", flowWorkbookPath);
		if (new File(flowWorkbookPath).isFile()) {
			workbook = new XSSFWorkbook(flowWorkbookPath);
			flowWorkbookReader = new WorkbookReader(workbook);

			for (Sheet sheet : workbook) {
				LOGGER.trace("Sheet: {}", sheet::getSheetName);
				if (new SheetPrefixValidator(sheet).validate()) {
					parseSheet(testCase, sheet);
				}
			}
		} else {
			throw new Exception(flowWorkbookPath + " is not a valid file!");
		}

		return testCase;
	}

	/**
	 * Parse the given sheet. Find tags and execute related actions to retrieve/set
	 * the data for this test case.
	 *
	 * @param testCase
	 * @param sheet
	 * @throws Exception
	 */
	private void parseSheet(TestCase testCase, Sheet sheet) throws Exception {
		MutableInt rowNum = new MutableInt(sheet.getFirstRowNum());

		LOGGER.trace("Parsing sheet '{}'", sheet.getSheetName());

		boolean currentTcidFound = false;
		boolean firstTagIsTcid = false;

		while (rowNum.intValue() < sheet.getLastRowNum()) {
			Row row = sheet.getRow(rowNum.intValue());
			if (row != null) {
				Cell firstCell = row.getCell(0);
				if (firstCell != null) {
					String firstCellContent = flowWorkbookReader.evaluateCellContent(firstCell, dateFormat);
					if (flowWorkbookReader.isTag(firstCellContent)) {
						String tagName = flowWorkbookReader.getTagName(firstCellContent);
						LOGGER.trace("Found tag: {}", tagName);
						if (!firstTagIsTcid) {
							if (!tagName.equals("tcid")) {
								return; // ignore sheet
							}
							firstTagIsTcid = true;
						}

						if (tagName.equals("tcid")) {
							// read tcid value, it should be the same as current testcase id. If not, skip
							// following fields.
							String tcid = flowWorkbookReader.readProperty(row, firstCell);
							// get string after last / char.
							//String testCaseIdShort = testCase.getId().replaceAll(".+/", "");
							String testCaseIdShort = testCase.getId().substring(testCase.getId().lastIndexOf(File.separator)+1);
							if (testCaseIdShort.equals(tcid)) {
								currentTcidFound = true;
								LOGGER.info("Reading fields for Testcase definition with id {}", tcid);
							} else {
								LOGGER.debug("datasetName = " + dataSetName);
								if (testCaseIdShort.equals(dataSetName)){
									currentTcidFound = true;
									LOGGER.info("Reading fields for Testcase definition with id {}", dataSetName);
								}
								else
								{
									currentTcidFound = false;
									LOGGER.info("Skipping fields for Testcase definition with id {}", tcid);
								}
							}
						} else {

							if (currentTcidFound) {
								switch (tagName) {
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
									testCase.setStatus(
											TestStatus.valueOf(flowWorkbookReader.readProperty(row, firstCell)));
									break;
								case "profile":
									readSelectedProfile(this.workbook, sheet, rowNum);
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
				}
			}
			rowNum.increment();
		}
	}

	/**
	 * Parse the given sheet. Find tags and execute related actions to retrieve/set
	 * the data for this test case.
	 *
	 * @param sheet
	 * @param rowNum
	 * @param tagname
	 * @throws Exception
	 */
	private List<TestAction> parseSoapDbTemplate(Sheet sheet, MutableInt rowNum, String tagName,
			List<Properties> dataset, String specifiedPrefix) throws Exception {
		Map<Integer, String> headers = null;
		Map<String, String> rowMap = null;
		TestAction testAction = null;
		List<TestAction> testActions = new ArrayList<>();
		String type = null;
		ActionFactory actionFactory = null;

		if (new SheetPrefixValidator(sheet).validate("DB")) {
			type = "DB";
			actionFactory = new DbActionFactory();
		} else if (new SheetPrefixValidator(sheet).validate("SOAP")) {
			type = "SOAP";
			actionFactory = new SoapActionFactory();
		}

		LOGGER.info("Parsing " + type + " template with sheet " + sheet.getSheetName());

		// Add input properties to the central properties collection
		if (dataset != null) {
			LOGGER.debug("Adding " + dataset.get(0).size() + " properties from testdata.");
			for (Entry entry : dataset.get(0).entrySet()) {
				String key = (String) entry.getKey();
				String val = (String) entry.getValue();
				testRunner.setPropertyValue(key.trim(), val.trim());
			}
		} else {
			LOGGER.debug("Dataset from testdata is empty, continuing.");
		}

		// Read and process the specified part of the SOAP/DB template based on the
		// tagname. Each tagname
		// is linked to a unique TestAction. Together they form the basis for sending
		// and testing.
		if (tagName.toLowerCase().equals("poll")) {
			//TestAction execute = actionFactory.getAction("POLLFOR" + type + "RESULT");
			//execute.setTestRunner(testRunner);
			//testActions.add(execute);
			testAction = actionFactory.getAction("POLLFOR" + type + "RESULT");
			testAction.setTestRunner(testRunner);
		}

		else if (tagName.toLowerCase().equals("execute") | tagName.toLowerCase().equals("send")) {
			//TestAction execute = actionFactory.getAction("PROCESS" + type + "RESPONSE");
			//execute.setTestRunner(testRunner);
			//testActions.add(execute);
			testAction = actionFactory.getAction("PROCESS" + type + "RESPONSE");
			testAction.setTestRunner(testRunner);

			// After adding execute TestAction, add tests from datasheet if available.
			if (dataset != null) {
				if (!dataset.isEmpty()) {
					for (Entry entry : dataset.get(1).entrySet()) {
						try {
							TestAction test = actionFactory.getAction("EXECUTE" + type + "TEST");
							test.setAttribute((String) entry.getKey(), (String) entry.getValue());
							test.setTestRunner(testRunner);
							testActions.add(test);
						} catch (Exception eX) {
							LOGGER.error("Exception while setting attribute!" + eX.getMessage());
							eX.printStackTrace();
						}
					}
				}
			}
		} else if (tagName.toLowerCase().equals("query") | tagName.toLowerCase().equals("message")) {
			testAction = actionFactory.getAction("SET" + type + "QUERY");
			headers = readSectionHeaderRow(sheet, rowNum);
			messageOrQuery = "";

			while ((rowMap = readRow(sheet, rowNum, headers)) != null) {
				if (rowMap.isEmpty()) {
					LOGGER.debug("End of query/message block reached at row " + rowNum);
					break;
				}
				for (String entry : rowMap.values()) {
					if (entry != null & entry.length() > 0) {
						LOGGER.trace("Processing cell value : '{}'", entry);
						String processedCellValue = testRunner.replaceKeywords(entry);
						messageOrQuery += processedCellValue + " ";
					}
				}
			}
			if (testRunner.containsKeywords(messageOrQuery)) {
				messageOrQuery = testRunner.replaceKeywords(messageOrQuery);
			}
			testAction.setAttribute("QUERY", messageOrQuery);
			messageOrQuery = "";
		} else if (tagName.toLowerCase().equals("queryproperties") | tagName.toLowerCase().equals("soapproperties")) {
			testAction = actionFactory.getAction("SET" + type + "PROPERTIES");
			headers = readSectionHeaderRow(sheet, rowNum);
			String prefix = null;

			while ((rowMap = readRow(sheet, rowNum, headers)) != null) {
				// Check for prefix on current row. If found then check for existence in
				// global properties with most recent incrementation (e.g. "SOAP_message10_")

				// PREFIX
				if (rowMap.containsKey("prefix") | specifiedPrefix != null) {
					LOGGER.trace("Prefix found. Processing.");

					if (specifiedPrefix != null) {
						prefix = specifiedPrefix;
					} else {
						prefix = rowMap.get("prefix");
					}

					Boolean prefixFound = true;
					Integer counter = 1;

					while (prefixFound) {
						if (this.testRunner.hasPropertyValue("prova.properties.used." + prefix + counter)) {
							counter++;
						} else {
							LOGGER.trace(
									"Prefix of type '" + prefix + "' has highest incrementation number: " + counter);
							prefix = prefix + counter;
							LOGGER.trace("Setting prefix to " + prefix + " on key prova.properties.prefix");
							prefixFound = false;
						}
					}
					// Adding prefix as key to properties so properties can be scanned for
					// existence.
					this.testRunner.setPropertyValue("prova.properties.used." + prefix, prefix);
					// Setting prefix as current prefix on key prova.properties.prefix.
					testAction.setAttribute("prova.properties.prefix", prefix);
				} else {
					throw new Exception(
							"No prefix has been supplied! Please add a 'Prefix' key and value below your Properties tag.");
				}

				// PASSWORD
				if (rowMap.containsKey("password")) {
					LOGGER.trace("Password found. Processing.");
					if (CellReader.isKey(rowMap.get("password"))) {
						LOGGER.trace("Password value is a key, retrieving property value.");
						String password = this.testRunner
								.getPropertyValue(CellReader.getKeyName(rowMap.get("password")));
						if (password != null) {
							testAction.setAttribute("prova.properties.password", password);
						} else {
							LOGGER.debug("Unable to process password tag");
						}
					} else {
						testAction.setAttribute("prova.properties.password", rowMap.get("password"));
					}
				}

				// USER
				if (rowMap.containsKey("user")) {
					LOGGER.trace("User found. Processing.");
					if (CellReader.isKey(rowMap.get("user"))) {
						LOGGER.trace("User value is a key, retrieving property value.");
						String user = this.testRunner.getPropertyValue(CellReader.getKeyName(rowMap.get("user")));
						if (user != null) {
							testAction.setAttribute("prova.properties.user", user);
						} else {
							LOGGER.debug("Unable to process user tag");
						}
					} else {
						testAction.setAttribute("prova.properties.user", rowMap.get("user"));
					}
				}

				// ROLLBACK
				if (rowMap.containsKey("rollback")) {
					LOGGER.trace("Rollback found. Processing.");
					String rollback = rowMap.get("rollback").trim().toLowerCase();
					if (!(rollback.equals("false") || rollback.equals("true"))) {
						throw new Exception("Rollback value must be 'false' or 'true'!");
					}
					testAction.setAttribute("prova.properties.rollback", rollback);
				}

				// ADDRESS or URL
				if (rowMap.containsKey("address") | rowMap.containsKey("url")) {
					String addressOrUrl = rowMap.containsKey("address") ? "address" : "url";
					LOGGER.trace(addressOrUrl + " found. Processing.");
					if (CellReader.isKey(rowMap.get(addressOrUrl))) {
						LOGGER.trace("Address or URL value is a key, retrieving property value.");
						String addressOrUrlPropertyValue = this.testRunner
								.getPropertyValue(CellReader.getKeyName(rowMap.get(addressOrUrl)));
						if (addressOrUrlPropertyValue != null) {
							testAction.setAttribute("prova.properties." + addressOrUrl, addressOrUrlPropertyValue);
						} else {
							LOGGER.debug("Unable to process address tag");
						}
					} else {
						testAction.setAttribute("prova.properties." + addressOrUrl, rowMap.get(addressOrUrl));
					}
				} else {
					throw new Exception(
							"No address or URL has been supplied! Please add a 'Address' or 'URL' key and value below your Properties tag.");
				}
			}
		} else if (tagName.toLowerCase().equals("pollproperties")) {
			testAction = actionFactory.getAction("SET" + type + "POLLPROPERTIES");
			headers = readSectionHeaderRow(sheet, rowNum);

			while ((rowMap = readRow(sheet, rowNum, headers)) != null) {
				if (rowMap.containsKey("retries")) {
					LOGGER.trace("Retries found. Processing.");
					testAction = parseProperty(rowMap, "retries", testAction);
				}
				if (rowMap.containsKey("waittime")) {
					LOGGER.trace("Waittime found. Processing.");
					testAction = parseProperty(rowMap, "waittime", testAction);
				}
				if (rowMap.containsKey("result")) {
					LOGGER.trace("Result found. Processing.");
					testAction = parseProperty(rowMap, "result", testAction);
				}
			}
		}
		if (testAction != null) {
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
	private void parseTestCaseSection(TestCase testCase, Sheet sheet, MutableInt rowNum, String tagName)
			throws Exception {
		Map<Integer, String> headers = readSectionHeaderRow(sheet, rowNum);
		Map<String, String> rowMap;

		LOGGER.trace("Parsing testcase section on sheet '{}'", sheet.getSheetName());

		while ((rowMap = readRow(sheet, rowNum, headers)) != null) {
			switch (tagName) {
			case "labels":
				// TODO process label
				break;
			case "profile":
				readSelectedProfile(this.workbook, sheet, rowNum);
				break;
			case "setup":
				readTestActionsFromReference(testCase, rowMap, "setup").forEach(testCase::addSetUpAction);
				break;
			case "test":
				readTestActionsFromReference(testCase, rowMap, "test").forEach(testCase::addTestAction);
				break;
			case "teardown":
				readTestActionsFromReference(testCase, rowMap, "teardown").forEach(testCase::addTearDownAction);
				break;
			}
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
	private void parseIntegratedTestCaseSection(TestCase testCase, Sheet sheet, MutableInt rowNum, String tagName)
			throws Exception {
		Map<Integer, String> headers = readSectionHeaderRow(sheet, rowNum);
		Map<String, String> rowMap;

		LOGGER.trace("Parsing integrated testcase section on sheet '{}'", sheet.getSheetName());

		while ((rowMap = readRow(sheet, rowNum, headers)) != null) {
			switch (tagName) {
			case "labels":
				// TODO process label
				break;
			case "integratetest":
			case "it":
				readTestActionsFromReference(testCase, rowMap, "test").forEach(testCase::addTestAction);
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
	private List<TestAction> readTestActionsFromReference(TestCase testCase, Map<String, String> rowMap, String type)
			throws Exception {
		Sheet nextSheet;

		if (rowMap.get("package").isEmpty()) {
			LOGGER.debug("Find sheet {} in same workbook", rowMap.get(type));
			nextSheet = workbook.getSheet(rowMap.get(type));

			if (nextSheet == null) {
				throw new Exception("Sheet " + rowMap.get(type) + " not found in workbook " + flowWorkbookPath);
			}
		} else {
			LOGGER.debug("Find sheet {} in package {}", rowMap.get(type), rowMap.get("package"));
			String nextPath = getWorkbookFromPackage(rowMap.get("package"));

			if (!new File(nextPath).exists()) {
				throw new Exception("Workbook '" + nextPath + "' not found");
			}

			nextSheet = new XSSFWorkbook(nextPath).getSheet(rowMap.get(type));

			if (nextSheet == null) {
				throw new Exception("Sheet " + rowMap.get(type) + " not found in workbook " + nextPath);
			}
		}

		// In case the sheet is of type 'SOAP' or 'DB', (optional) testdata is collected
		// and used for iteration over the sheet.
		if (new SheetPrefixValidator(nextSheet).validate("SOAP")
				|| new SheetPrefixValidator(nextSheet).validate("DB")) {
			String specificSheet = null;
			ArrayList<List<Properties>> testData;
			List<TestAction> testActions = new ArrayList<>();
			// If the TestdataSheet column exists and is filled in, the sheet to be used
			if (rowMap.containsKey("testdatasheet")) {
				if (!(rowMap.get("testdatasheet").isEmpty())) {
					specificSheet = rowMap.get("testdatasheet");
					testData = getSoapDbTestdata(testCase, nextSheet, specificSheet);
					if (testData.size() > 0) {
						// If multiple sets of testdata are defined, the 'template' of the soap message
						// or db query is ran with each set of testdata.
						LOGGER.info(testData.size() + " sets of testdata found for sheet '" + nextSheet.getSheetName()
								+ "'");
						for (List<Properties> dataSet : testData) {
							testActions.addAll(readTestActionsFromSheet(testCase, nextSheet, dataSet, specificSheet));
						}
						return testActions;
					}
				}
			}

			else {
				LOGGER.info("No testdata found for sheet '" + nextSheet.getSheetName() + "', processing once.");
				testActions.addAll(readTestActionsFromSheet(testCase, nextSheet, null, null));
				return testActions;
			}
		}

		// TODO READ keywords for reference sheet!
		return readTestActionsFromSheet(testCase, nextSheet, null, null);
	}

	/**
	 * Scan an imported sheet for test actions
	 *
	 * @param sheet
	 * @return
	 * @throws Exception
	 */
	private List<TestAction> readTestActionsFromSheet(TestCase testCase, Sheet sheet, List<Properties> dataSet,
			String specifiedPrefix) throws Exception {
		List<TestAction> testActions = new ArrayList<>();
		MutableInt rowNum = new MutableInt(sheet.getFirstRowNum());

		while (rowNum.intValue() < sheet.getLastRowNum()) {
			Row row = sheet.getRow(rowNum.intValue());

			if (row != null) {
				Cell firstCell = row.getCell(0);
				if (firstCell != null) {
					String firstCellContent = flowWorkbookReader.evaluateCellContent(firstCell, dateFormat);

					if (flowWorkbookReader.isTag(firstCellContent)) {
						// get tag
						String tagName = flowWorkbookReader.getTagName(firstCellContent);
						LOGGER.trace("Found tag: {}", tagName);

						switch (tagName) {
						case "sectie":
						case "tc":
							parseTestActionSection(testCase, testActions, sheet, rowNum, tagName);
							break;
						case "query":
						case "queryproperties":
						case "pollproperties":
						case "execute":
						case "poll":
						case "message":
						case "soapproperties":
						case "send":
							parseSoapDbTemplate(sheet, rowNum, tagName, dataSet, specifiedPrefix)
									.forEach(testCase::addTestAction);
							break;
						case "command":
							testActions.add(parseShellTemplate(sheet, rowNum, tagName));
							break;
						case "integratetest":
						case "it":
							testActions.forEach(testCase::addTestAction);
							testActions.clear();
							parseIntegratedTestCaseSection(testCase, sheet, rowNum, tagName);
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
	 * Scan all rows on the given <sheet> and parse all actions and import
	 * referenced sheets.
	 *
	 * @param testActions
	 * @param sheet
	 * @param rowNum
	 * @param tagName
	 * @throws Exception
	 */
	private void parseTestActionSection(TestCase testCase, List<TestAction> testActions, Sheet sheet, MutableInt rowNum,
			String tagName) throws Exception {
		flowWorkbookReader = new WorkbookReader(sheet.getWorkbook());
		// get header row
		Map<Integer, String> headers = readSectionHeaderRow(sheet, rowNum);
		Map<String, String> rowMap;
		String keyword;
		ActionFactory actionFactory = new WebActionFactory();

		while ((rowMap = readRow(sheet, rowNum, headers)) != null) {
			switch (tagName) {
			case "sectie":
				TestAction testAction = actionFactory.getAction(rowMap.get("actie"));
				String locatorName = rowMap.get("locator").toLowerCase();
				String xPath = "";

				if (testRunner.hasPropertyValue(Config.PROVA_PLUGINS_OUT_WEB_LOCATOR_PFX + "." + locatorName)) {
					xPath = testRunner.getPropertyValue(Config.PROVA_PLUGINS_OUT_WEB_LOCATOR_PFX + "." + locatorName);
					testAction.setAttribute("xpath", xPath);
				}

				LOGGER.trace("Action: '{}', Locator: '{}' (xpath: {})", rowMap.get("actie").toUpperCase(), locatorName,
						xPath);
				LOGGER.trace("Sheet {}, Rownumber {}", sheet.getSheetName(), rowNum);

				testAction.setTestRunner(testRunner);
				testAction.setId(sheet.getSheetName() + " | #" + ((rowNum.toInteger()) + 1));

				for (String key : rowMap.keySet()) {
					if (!key.equals("actie")) {
						keyword = rowMap.get(key);

						if (keyword.length() > 2 && keyword.startsWith("{") && keyword.endsWith("}")) {
							// Remove the { } around the keyword
							keyword = keyword.trim().substring(1, keyword.length() - 1);

							if (testDataKeywords.containsKey(keyword)) {
								LOGGER.trace("Substitute key '{}'. Keyword '{}' with value '{}'", key, keyword,
										testDataKeywords.getProperty(keyword));
								keyword = testDataKeywords.getProperty(keyword);
							} else if (testRunner.hasPropertyValue(keyword)) {
								keyword = testRunner.getPropertyValue(keyword);
							} else {
								if (this.testRunner.hasPropertyValue(Config.PROVA_FLOW_FAILON_NOTESTDATAKEYWORD)) {
									Boolean failOnNoTestdataKeywords = false;

									try {
										failOnNoTestdataKeywords = Boolean.parseBoolean(this.testRunner
												.getPropertyValue(Config.PROVA_FLOW_FAILON_NOTESTDATAKEYWORD));
									} catch (Exception ex) {
										LOGGER.error("Error parsing property '{}', please check your property file.",
												Config.PROVA_FLOW_FAILON_NOTESTDATAKEYWORD);
									}

									if (failOnNoTestdataKeywords) {
										throw new Exception("Keyword '" + keyword + "' in sheet '"
												+ sheet.getSheetName() + "' not defined with a value.");
									} else {
										LOGGER.error("Keyword '" + keyword + "' in sheet '" + sheet.getSheetName()
												+ "' not defined with a value.");
										keyword = rowMap.get(key);
									}
								}
							}
						}

						testAction.setAttribute(key, keyword);
						LOGGER.trace("Read '{}' action attribute '{}' = '{}'", rowMap.get("actie").toUpperCase(), key,
								keyword);
					}
				}
				LOGGER.trace("Read '{}' action '{}'", rowMap.get("actie"), testAction);
				testActions.add(testAction);
				break;
			case "tc":
				if (rowMap.containsKey("test")) {
					readTestActionsFromReference(testCase, rowMap, "test").forEach(testActions::add);
				} else if (rowMap.containsKey("setup")) {
					readTestActionsFromReference(testCase, rowMap, "setup").forEach(testActions::add);
				} else if (rowMap.containsKey("teardown")) {
					readTestActionsFromReference(testCase, rowMap, "teardown").forEach(testActions::add);
				} else {
					LOGGER.error("No test, setup or teardown column found in section!");
				}
				break;
			}
		}
	}

	/**
	 * Parse an test action header and get all variable names for the test actions
	 *
	 * @param sheet
	 * @param rowNum
	 * @return
	 * @throws Exception
	 */
	private Map<Integer, String> readSectionHeaderRow(Sheet sheet, MutableInt rowNum) throws Exception {
		Map<Integer, String> headers = new HashMap<>();
		String header;

		rowNum.increment();
		Row headerRow = sheet.getRow(rowNum.intValue());

		if (headerRow != null) {
			for (Cell headerCell : headerRow) {
				header = flowWorkbookReader.evaluateCellContent(headerCell, dateFormat);
				header = header.replace("*", "");
				header = header.replace(":", "");
				header = header.toLowerCase();
				header = header.trim();

				if (!header.isEmpty()) {
					headers.put(headerCell.getColumnIndex(), header);
				}
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
	private Map<String, String> readRow(Sheet sheet, MutableInt rowNum, Map<Integer, String> headers) throws Exception {
		rowNum.increment();
		Row labelRow = sheet.getRow(rowNum.intValue());

		// break if row is empty
		if (labelRow == null) {
			return null;
		}

		Map<String, String> rowMap = new LinkedHashMap<>();
		Set<Map.Entry<Integer, String>> headerEntries = headers.entrySet();
		int numHeaderEntries = headerEntries.size();
		int numEmptyFields = 0;

		for (Map.Entry<Integer, String> headerEntry : headerEntries) {
			Cell labelCell = labelRow.getCell(headerEntry.getKey());
			if (labelCell != null) {
				if (flowWorkbookReader.evaluateCellContent(labelCell).length() >= 1) {
					LOGGER.trace("Adding value to map: Cellvalue: {} ",
							flowWorkbookReader.evaluateCellContent(labelCell));
					rowMap.put(headerEntry.getValue(), flowWorkbookReader.evaluateCellContent(labelCell, dateFormat));
				} else {
					rowMap.put(headerEntry.getValue(), "");
					numEmptyFields = numEmptyFields + 1;
				}
			} else {
				rowMap.put(headerEntry.getValue(), "");
				numEmptyFields = numEmptyFields + 1;
			}
		}
		if (numEmptyFields == numHeaderEntries) {
			LOGGER.trace("Row {} is empty, but not null. Probably some value is hidden in some fields",
					(rowNum.intValue()) + 1);
			return null;
		}

		// break if row map only contains nulls
		if (!CollectionUtils.exists(rowMap.values(), NotNullPredicate.INSTANCE)) {
			return null;
		}

		return rowMap;
	}

	/**
	 * Extract the path of the workbook containing the test flow from TCID
	 *
	 * @param tcid
	 * @return
	 */
	private String getFlowPathFromTCID(String tcid) {
		LOGGER.trace("Get flow path from TCID: '{}'", tcid);

		// Split the flow and data file (if exists in tcid)
		if (tcid.contains(File.separator + File.separator)) {
			tcid = tcid.substring(0, tcid.lastIndexOf(File.separator + File.separator));
		}

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
	private String getDataPathFromTCID(String tcid) throws Exception {
		LOGGER.trace("Get data path from TCID: '{}'", tcid);

		String path = getFlowPathFromTCID(tcid);
		String separator = File.separator + File.separator;

		path = path.substring(0, path.lastIndexOf(File.separator));

		// If tcid doesn't contain a data path then return with an empty string
		if (!tcid.contains(separator) || !tcid.contains(".xlsx")) {
			return "";
		}

		path += File.separator + this.testRunner.getPropertyValue(Config.PROVA_TESTS_DATA_DIR) + File.separator
				+ tcid.substring(tcid.lastIndexOf(separator) + separator.length(),
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
	private String getKeywordSetFromTCID(String tcid) throws Exception {
		LOGGER.trace("Get Keyword Set From TCID: '{}'", tcid);

		String keyWordSet = "";

		if (tcid.contains("xlsx")) {
			keyWordSet = tcid.substring(tcid.lastIndexOf(File.separator) + File.separator.length(), tcid.length());
		}

		LOGGER.debug("Found keyword set from TCID: '{}' (tcid)", keyWordSet, tcid);

		return keyWordSet;
	}

	/**
	 * Convert a package name to a correct filename
	 *
	 * @param _package
	 * @return
	 */
	private String getWorkbookFromPackage(String _package) {
		LOGGER.trace("getWorkbookFromPackage: '{}'", _package);

		return testRootPath + File.separator + _package.replace(".", File.separator) + ".xlsm";
	}

	/**
	 * For trace logging, print the data in given test map.
	 *
	 * @param testData
	 */
	@SuppressWarnings("unused")
	private void printTestDataMap(LinkedHashMap<String, Map<String, String>> testData) {
		LOGGER.trace("Print TestData Map with {} sets data:", testData.size());

		for (String key : testData.keySet()) {
			LOGGER.trace("Data set: '{}'", key);

			Map<String, String> map = testData.get(key);

			for (Entry<String, String> entry : map.entrySet()) {
				LOGGER.trace("> {}: '{}'", entry.getKey(), entry.getValue());
			}
		}
	}

	/**
	 * For trace logging, log all items in the property set
	 *
	 * @param props
	 */
	private void printTestDataKeywords(Properties props) {
		LOGGER.trace("Print TestData Properties with {} sets data:", props.size());

		for (String key : props.stringPropertyNames()) {
			LOGGER.trace("> " + key + " => " + props.getProperty(key));
		}
	}

	private ArrayList<List<Properties>> getSoapDbTestdata(TestCase testCase, Sheet sheet, String specificSheet)
			throws Exception {
		ArrayList<List<Properties>> testData = new ArrayList<>();
		String selectedSheet = sheet.getSheetName();

		if (specificSheet != null) {
			selectedSheet = specificSheet;
		}

		dataWorkbookPath = getFlowPathFromTCID(testCase.getId()).substring(0,
				getFlowPathFromTCID(testCase.getId()).lastIndexOf(File.separator));

		// Check if last character is a separator
		if ((dataWorkbookPath.charAt(dataWorkbookPath.length() - 1) + "").equals(File.separator)) {
			dataWorkbookPath += "testdata" + File.separator + sheet.getSheetName() + ".xlsx";
		} else {
			dataWorkbookPath += File.separator + "testdata" + File.separator + sheet.getSheetName() + ".xlsx";
		}

		// Load SOAP or DB specific testdata sheets
		if (new SheetPrefixValidator(sheet).validate("SOAP") || new SheetPrefixValidator(sheet).validate("DB")) {
			if (new File(dataWorkbookPath).isFile()) {
				LOGGER.debug("Reading SOAP/DB testdata sheet: '" + dataWorkbookPath + "'");
				testData = new TestDataBuilder(testRunner).buildTestDataAndTests(dataWorkbookPath, selectedSheet);
				if (testData.isEmpty()) {
					LOGGER.debug(
							"No testdata returned for path " + dataWorkbookPath + " and sheetname " + selectedSheet);
				}
			} else {
				LOGGER.debug("No testdata file found for path " + dataWorkbookPath + " and sheetname " + selectedSheet);
			}
		}
		return testData;
	}

	private void readSelectedProfile(Workbook workbook, Sheet sheet, MutableInt rowNum) throws Exception {
		// First read the selected profile from the testcase sheet

		Row profileRow = sheet.getRow(rowNum.intValue());
		Cell profile = profileRow.getCell(1);
		String profileValue = "";
		if (flowWorkbookReader.isKey(profile.getStringCellValue())) {
			profileValue = flowWorkbookReader.getKeyName(profile.getStringCellValue());
			profileValue = this.testRunner.getPropertyValue(profileValue);
		} else {
			profileValue = profile.getStringCellValue();
		}
		LOGGER.info("Selected profile is '{}'", profileValue);

		Sheet provaProfiles = workbook.getSheet("Prova_Profiles");

		if (provaProfiles == null) {
			throw new Exception("Sheet Prova_Profiles not available, please check testcase '" + sheet.getSheetName()
					+ "' workbook.");
		} else {
			LOGGER.debug("Collecting profile properties from Prova_Properties sheet");
			Map<Integer, String> headers = readSectionHeaderRow(provaProfiles, new MutableInt(-1));
			Properties selectedProfileProperties = null;

			// Read all the profile properties from specified column
			for (Entry column : headers.entrySet()) {
				if (column.getValue().toString().equalsIgnoreCase(profileValue)) {
					int start = provaProfiles.getRow(1).getCell(0) == null ? 1 : 2;
					selectedProfileProperties = new TestDataBuilder(testRunner).readColumn(provaProfiles,
							(Integer) column.getKey(), start, provaProfiles.getLastRowNum(),
							new WorkbookReader(workbook));
				}
			}
			if (selectedProfileProperties == null) {
				throw new Exception("Profile " + profileValue + " not found.");
			} else {
				if (!selectedProfileProperties.isEmpty()) {
					for (Entry prop : selectedProfileProperties.entrySet()) {
						String value = null;

						if (flowWorkbookReader.isKey((String) prop.getValue())) {
							value = flowWorkbookReader.getKeyName((String) prop.getValue());
						} else {
							value = (String) prop.getValue();
						}

						// Store all the profile properties in the central properties object after
						// looking for possible property value
						if (value == null)
							continue;

						if (this.testRunner.hasPropertyValue(value)) {
							value = this.testRunner.getPropertyValue(value);
						}
						this.testRunner.setPropertyValue((String) prop.getKey(), value);
					}
				} else {
					LOGGER.error("0 properties return from Prova_Profiles sheet, please check your file.");
				}
			}
		}
	}

	private TestAction parseShellTemplate(Sheet sheet, MutableInt rowNum, String tagName) throws Exception {
		ActionFactory actionFactory = new ShellActionFactory();
		TestAction testAction = null;
		Map<Integer, String> headers;
		Map<String, String> rowMap = null;
		String command = null;

		if (tagName.equalsIgnoreCase("command")) {
			LOGGER.info("Parsing shell template with sheet " + sheet.getSheetName());

			testAction = actionFactory.getAction("EXECUTE");
			headers = readSectionHeaderRow(sheet, rowNum);
			command = "";

			while ((rowMap = readRow(sheet, rowNum, headers)) != null) {
				if (rowMap.isEmpty()) {
					LOGGER.debug("End of command block reached at row " + rowNum);
					break;
				}
				for (String entry : rowMap.values()) {
					if (entry != null & entry.length() > 0) {
						LOGGER.trace("Processing cell value : '{}'", entry);
						String processedCellValue = testRunner.replaceKeywords(entry);
						command += processedCellValue + " ";
					}
				}
			}
			if (testRunner.containsKeywords(command)) {
				command = testRunner.replaceKeywords(command);
			}
			LOGGER.trace("Retrieved the following command: '{}'", command);
			testAction.setAttribute("COMMAND", command);
			testAction.setTestRunner(testRunner);

		}
		return testAction;
	}

	private TestAction parseProperty(Map<String, String> rowMap, String property, TestAction testAction) {
		try {
			if (CellReader.isKey(rowMap.get(property)))

			{
				LOGGER.trace(property + " value is a key, retrieving property value.");
				String value = this.testRunner.getPropertyValue(CellReader.getKeyName(rowMap.get(property)));
				if (value != null) {
					testAction.setAttribute("prova.properties." + property, value);
				} else {
					LOGGER.debug("Unable to process tag");
				}
			} else {
				testAction.setAttribute("prova.properties." + property, rowMap.get(property));
			}

		} catch (Exception eX) {
			LOGGER.error("Exception while setting property " + property + "!" + eX.getMessage());
			eX.printStackTrace();
		}
		return testAction;

	}
}
