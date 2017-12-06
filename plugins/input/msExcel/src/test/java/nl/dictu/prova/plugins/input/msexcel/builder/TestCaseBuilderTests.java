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

import nl.dictu.prova.framework.TestCase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

/**
 * Tests for TestCaseBuilder class
 *
 * @author Hielke de Haan
 */
public class TestCaseBuilderTests {
    private static final Logger LOGGER = LogManager.getLogger();
    private String testRoot;

    @Before
    public void setUp() throws Exception {
        testRoot = new File(this.getClass().getResource("../CellReaderTests.xlsx").getFile()).getParentFile().getPath()
                + File.separator + "tests";
    }

    @Test
    @Ignore
    public void testBuildTestCase1() throws Exception {
        TestCase testCase = new TestCase(testRoot
                + "/functional/projectSubsidies/verlening/AVBH/AVBH.xlsm/WEB_ADAC_001".replace("/", File.separator));
        testCase = new TestCaseBuilder(testRoot, null).buildTestCase(testCase);
        LOGGER.debug(testCase);
    }

    @Test(expected = Exception.class)
    public void testBuildTestCase2() throws Exception {
        TestCase testCase = new TestCase(
                testRoot + "/functional/deelname/Deelname.xlsm/WEB_DLN_001".replace("/", File.separator));
        testCase = new TestCaseBuilder(testRoot, null).buildTestCase(testCase);
        LOGGER.debug(testCase);
    }
}
