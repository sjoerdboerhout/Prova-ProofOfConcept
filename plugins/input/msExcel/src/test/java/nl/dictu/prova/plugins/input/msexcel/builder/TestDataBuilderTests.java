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
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for TestDataBuilder class
 *
 * @author Hielke de Haan
 */
public class TestDataBuilderTests {
    private static final Logger LOGGER = LogManager.getLogger();
    private String testRoot;

    @Before
    public void setUp() throws Exception {
        testRoot = new File(this.getClass().getResource("../CellReaderTests.xlsx").getFile()).getParentFile().getPath()
                + File.separator + "tests";
        LOGGER.info("TestRoot: '{}'", () -> testRoot);
    }

    @Test
    public void testBuildTestData() throws Exception {
        Properties testData = new TestDataBuilder().buildTestData(
                testRoot + "/functional/projectSubsidies/deelbetaling/ADAC/testdata/ADAC_WEB_ADAC_001.xlsx".replace("/",
                        File.separator),
                "TEST 01");

        for (String key : testData.stringPropertyNames()) {
            LOGGER.debug("> " + key + " => " + testData.getProperty(key));
        }
    }
}
