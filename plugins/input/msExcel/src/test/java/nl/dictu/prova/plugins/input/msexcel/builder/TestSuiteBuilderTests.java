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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import nl.dictu.prova.Prova;
import nl.dictu.prova.TestRunner;
import nl.dictu.prova.framework.TestAction;
import nl.dictu.prova.framework.TestCase;
import nl.dictu.prova.framework.TestSuite;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests for TestSuiteBuilder class
 *
 * @author Hielke de Haan
 */
public class TestSuiteBuilderTests {
	private final static Logger LOGGER = LogManager.getLogger();
	private String testRoot;

	@Before
	public void setUp() {
		testRoot = new File(this.getClass().getResource("../CellReaderTests.xlsx").getFile()).getParentFile().getPath()
				+ File.separator + "tests";
	}

	@Test
	@Ignore
	public void testBuildTestSuite() throws Exception {
		TestSuite testSuite = new TestSuiteBuilder().buildTestSuite(new File(testRoot), null);
		logTestSuite(testSuite);
	}

	private void logTestSuite(TestSuite testSuite) {
		LOGGER.debug(testSuite.getId() + " (TS)");

		LinkedHashMap<String, TestCase> testCases = testSuite.getTestCases();
		for (Map.Entry<String, TestCase> entry : testCases.entrySet())
			LOGGER.debug(entry.getValue().getId() + " (TC)");

		LinkedHashMap<String, TestSuite> testSuites = testSuite.getTestSuites();
		for (Map.Entry<String, TestSuite> entry : testSuites.entrySet())
			logTestSuite(entry.getValue());
	}

	@Test
	@Ignore
	public void testCaseBuildMultiplePerWorkbook() throws Exception {
		String testRootFile =  testRoot + "/functional/gebeurtenissen";
		
//		testRootFile = "/home/user/klap/prova-klap.git/prova-test/projects/mijnrvo-test/";
		testRootFile = "/media/sf_CData/prova-klap.git/prova-test/projects/mijnrvo-test";
		TestSuite testSuite = new TestSuiteBuilder().buildTestSuite(new File(testRootFile),
				null);
		logTestSuite(testSuite);
		
		TestRunner testRunner = new Prova() {
			@Override
			public String getPropertyValue(String key) throws Exception {
				
				try {
					return super.getPropertyValue(key);
				} catch (Exception e) {
					LOGGER.debug("property {} not found",key);
					return "dummy";
				}
			}
			
			@Override
			public Boolean hasPropertyValue(String key) {
				return true;
			}
		};
		testRunner.setPropertyValue("prova.env", "o");
		
		
		// build testactions for two TestCases.
		LinkedHashMap<String, TestCase> testCases = testSuite.getTestCases();
		for (Map.Entry<String, TestCase> entry : testCases.entrySet()) {
			TestCase testCase = entry.getValue();
			testCase = new TestCaseBuilder(testRootFile, testRunner).buildTestCase(testCase);
			logTestCase(testCase);
		}
	}

	private void logTestCase(TestCase testCase) {
		LOGGER.debug("QQQ-startlogtestcase " + testCase.getId() + " (TestCase)");
		
		List<TestAction> testActions = testCase.getTestActions();
		LOGGER.debug("stats: {} #actions",testActions.size());
		for (TestAction testAction : testActions) {
			LOGGER.debug(testAction.getId() + " (TAction)");
		}
	}

}
