package nl.dictu.prova.plugins.input.msexcel.builder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Tests for TestDataBuilder class
 *
 * @author Hielke de Haan
 */
public class TestDataBuilderTests
{
  private static final Logger LOGGER = LogManager.getLogger();
  private String testRoot;

  @Before
  public void setUp() throws Exception
  {
    testRoot = new File(this.getClass().getResource("../CellReaderTests.xlsx").getFile()).getParentFile().getPath()
            + File.separator + "tests";
    LOGGER.info("TestRoot: '{}'", () -> testRoot);
  }

  @Test
  public void testBuildTestData() throws Exception
  {
    Properties testData = new TestDataBuilder().buildTestData(testRoot + "/functional/projectSubsidies/deelbetaling/ADAC/testdata/ADAC_WEB_ADAC_001.xlsx".replace("/", File.separator), "TEST 01");
    
    for(String key : testData.stringPropertyNames())
    {
      LOGGER.debug("> " + key + " => " + testData.getProperty(key));
    }
  }
}
