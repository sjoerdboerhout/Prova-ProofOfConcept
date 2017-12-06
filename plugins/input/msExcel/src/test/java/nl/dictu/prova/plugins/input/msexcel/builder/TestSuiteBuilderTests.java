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

import nl.dictu.prova.TestRunner;
import nl.dictu.prova.framework.TestCase;
import nl.dictu.prova.framework.TestSuite;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

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

}
