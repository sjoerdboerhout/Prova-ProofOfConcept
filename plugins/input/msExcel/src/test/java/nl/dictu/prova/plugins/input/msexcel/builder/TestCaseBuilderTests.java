package nl.dictu.prova.plugins.input.msexcel.builder;

import nl.dictu.prova.framework.TestCase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

/**
 * Tests for TestCaseBuilder class
 *
 * @author Hielke de Haan
 */
public class TestCaseBuilderTests
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
  public void testBuildTestCase1() throws Exception
  {
    TestCase testCase = new TestCase(testRoot + "/functional/projectSubsidies/verlening/AVBH/AVBH.xlsm/WEB_ADAC_001".replace("/", File.separator));
    testCase = new TestCaseBuilder(testRoot).buildTestCase(testCase);
    LOGGER.debug(testCase);
  }

  @Test(expected = Exception.class)
  public void testBuildTestCase2() throws Exception
  {
    TestCase testCase = new TestCase(testRoot + "/functional/deelname/Deelname.xlsm/WEB_DLN_001".replace("/", File.separator));
    testCase = new TestCaseBuilder(testRoot).buildTestCase(testCase);
    LOGGER.debug(testCase);
  }
}
