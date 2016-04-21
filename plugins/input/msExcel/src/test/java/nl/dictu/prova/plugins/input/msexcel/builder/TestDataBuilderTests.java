package nl.dictu.prova.plugins.input.msexcel.builder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

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
  }

  @Test
  public void testBuildTestData() throws Exception
  {
    LinkedHashMap<String, Map<String, String>> testData = new TestDataBuilder().buildTestData(testRoot + "/functional/projectSubsidies/deelbetaling/ADAC/testdata/ADAC_WEB_ADAC_001.xlsx".replace("/", File.separator));
    LOGGER.trace(testData);
  }
}
