package nl.dictu.prova.plugins.input.msexcel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import nl.dictu.prova.TestRunner;
import nl.dictu.prova.framework.TestCase;
import nl.dictu.prova.plugins.input.InputPlugin;

/**
 * Hello world!
 *
 */
public class MsExcel implements InputPlugin
{
  final static Logger LOGGER = LogManager.getLogger();
  
  @Override
  public void init(TestRunner testRunner) throws Exception
  {
    // TODO Auto-generated method stub
    LOGGER.debug("Init: input plugin MsExcel reader!");
    
    if(testRunner == null)
       throw new Exception("No testRunner supplied!");
  }

  @Override
  public void setTestRoot(String testRoot) throws Exception
  {
    // TODO Auto-generated method stub

  }

  @Override
  public void setLabels(String[] labels)
  {
    // TODO Auto-generated method stub

  }

  @Override
  public void setTestCaseFilter(String[] testCases)
  {
    // TODO Auto-generated method stub

  }

  @Override
  public void setUp() throws Exception
  {
    // TODO Auto-generated method stub
    LOGGER.debug("SetUp input plugin MsExcel");
  }

  @Override
  public void loadTestCase(TestCase testCase) throws Exception
  {
    // TODO Auto-generated method stub

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
