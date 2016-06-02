package nl.dictu.prova.plugins.reporting.simplereport;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import nl.dictu.prova.TestRunner;
import nl.dictu.prova.framework.TestAction;
import nl.dictu.prova.framework.TestCase;
import nl.dictu.prova.framework.TestSuite;
import nl.dictu.prova.plugins.reporting.ReportingPlugin;

/*
 * Hello world!
 *
 */
public class SimpleReport implements ReportingPlugin
{
  final static Logger LOGGER = LogManager.getLogger();
  
  private TestRunner testRunner;

  @Override
  public void init(TestRunner testRunner) throws Exception
  {
    LOGGER.debug("Init: reporting plugin Simple Report!");
    
    if(testRunner == null)
       throw new Exception("No testRunner supplied!");
    
    this.testRunner = testRunner;
  }

  @Override
  public void setOutputDir(String outputDir) throws Exception
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void setUp() throws Exception
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void logStartTest(TestCase testCase) throws Exception
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void logAction(TestAction action) throws Exception
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void logEndTest(TestCase testCase) throws Exception
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void logMessage(String message) throws Exception
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void logMessage(String[] messages) throws Exception
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void shutDown()
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void logStartTestSuite(TestSuite testSuite) throws Exception
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void logEndTestSuite(TestSuite testSuite) throws Exception
  {
    // TODO Auto-generated method stub
    
  }
}
