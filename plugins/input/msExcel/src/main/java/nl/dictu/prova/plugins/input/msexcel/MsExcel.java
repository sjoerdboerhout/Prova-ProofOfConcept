package nl.dictu.prova.plugins.input.msexcel;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import nl.dictu.prova.Config;
import nl.dictu.prova.TestRunner;
import nl.dictu.prova.framework.TestCase;
import nl.dictu.prova.framework.TestSuite;
import nl.dictu.prova.plugins.input.InputPlugin;
import nl.dictu.prova.plugins.input.msexcel.builder.TestCaseBuilder;
import nl.dictu.prova.plugins.input.msexcel.builder.TestSuiteBuilder;

/**
 * Input plugin for Prova for reading test scripts from MS Excel files
 * 
 * @author Sjoerd Boerhout
 * @since 2016-04-25
 */
public class MsExcel implements InputPlugin
{
  final static Logger LOGGER = LogManager.getLogger();
  
  private TestRunner testRunner = null;
  
  private File       testRoot   = null;
  private String[]   labels     = null;
  
  /**
   * Init the plug-in and check if a valid reference to a testRunner
   * was given
   */
  @Override
  public void init(TestRunner testRunner) throws Exception
  {
    LOGGER.debug("Init: input plugin MsExcel reader!");
    
    if(testRunner == null)
       throw new Exception("No testRunner supplied!");
    
    this.testRunner = testRunner;
  }

  
  /**
   * Set the root location of the test scripts and validate
   * it can be read correctly.
   */
  @Override
  public void setTestRoot(String newTestRoot) throws Exception
  {
    try
    {
      File testRoot = new File(newTestRoot);
      
      if(!testRoot.isDirectory())
      {
        LOGGER.trace("Test root not found. Try as sub-directory of Prova's default test root.");
        testRoot = new File(testRunner.getPropertyValue(Config.PROVA_DIR) + newTestRoot);
      }
      
      if(!testRoot.isDirectory())
        throw new Exception("Test root must be a directory! (" + newTestRoot + ")");
      
      if(!testRoot.canRead())
        throw new Exception("Test root can not be read! (" + newTestRoot + ")");
      
      LOGGER.trace("Update test root to '{}'", testRoot.getAbsolutePath() + "/");
      testRunner.setPropertyValue(Config.PROVA_TESTS_ROOT, testRoot.getAbsolutePath() + "/");
      
    }
    catch(Exception eX)
    {
      eX.printStackTrace();
      throw eX;
    }
  }

  @Override
  public void setLabels(String[] labels)
  {
    LOGGER.debug("Set filters for labels to '{}'", () -> labels);
    this.labels = labels;
  }

  @Override
  public void setTestCaseFilter(String[] testCases)
  {
    // TODO Auto-generated method stub

  }

  @Override
  public void setUp() throws Exception
  {
    LOGGER.debug("SetUp: input plugin MsExcel reader!");
    
    try
    {
      TestSuiteBuilder testSuiteBuilder = new TestSuiteBuilder();
      TestSuite testSuite = null;
      
      testSuite = testSuiteBuilder.buildTestSuite(new File(testRunner.getPropertyValue(Config.PROVA_TESTS_ROOT)));
      
      testRunner.setRootTestSuite(testSuite);
    }
    catch(Exception eX)
    {
      throw eX;
    }
  }

  
  @Override
  public void loadTestCase(TestCase testCase) throws Exception
  {
    // TODO Auto-generated method stub
    LOGGER.debug("LOAD TC: '{}'", () -> testCase.toString());
    
    TestCaseBuilder testCaseBuilder = new TestCaseBuilder(testRunner.getPropertyValue(Config.PROVA_TESTS_ROOT), this.testRunner);
    
    testCaseBuilder.buildTestCase(testCase);
    LOGGER.debug("LOADED TC: '{}'", () -> testCase.toString());
  }

  @Override
  public void shutDown()
  {
    // TODO Auto-generated method stub
    LOGGER.debug("ShutDown input plugin MsExcel");
  }

  @Override
  public String getName()
  {
    return "MsExcel";
  }
}
