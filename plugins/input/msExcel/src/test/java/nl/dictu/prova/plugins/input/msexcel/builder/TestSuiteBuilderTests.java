package nl.dictu.prova.plugins.input.msexcel.builder;

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
public class TestSuiteBuilderTests
{
  private final static Logger LOGGER = LogManager.getLogger();
  private String testRoot;

  @Before
  public void setUp()
  {
    testRoot = new File(this.getClass().getResource("../CellReaderTests.xlsx").getFile()).getParentFile().getPath()
            + File.separator + "tests";
  }

  @Test
  public void testBuildTestSuite() throws Exception
  {
    TestSuite testSuite = new TestSuiteBuilder().buildTestSuite(new File(testRoot));
    logTestSuite(testSuite);
  }

  private void logTestSuite(TestSuite testSuite)
  {
    LOGGER.debug(testSuite.getId() + " (TS)");

    LinkedHashMap<String, TestCase> testCases = testSuite.getTestCases();
    for (Map.Entry<String, TestCase> entry : testCases.entrySet())
      LOGGER.debug(entry.getValue().getId() + " (TC)");

    LinkedHashMap<String, TestSuite> testSuites = testSuite.getTestSuites();
    for (Map.Entry<String, TestSuite> entry : testSuites.entrySet())
      logTestSuite(entry.getValue());
  }

}
