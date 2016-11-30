package nl.dictu.prova.plugins.reporting;

import nl.dictu.prova.TestRunner;
import nl.dictu.prova.framework.TestAction;
import nl.dictu.prova.framework.TestCase;
import nl.dictu.prova.framework.TestSuite;

/**
 * Describes the functions that must be available for the other parts of the 
 * framework to report results of their actions.
 * 
 * @author  Sjoerd Boerhout
 * @since   0.0.1
 */
public interface ReportingPlugin
{
  public void init(TestRunner testRunner) throws Exception;
  
  public void setUp(String projectName) throws Exception;
  
  public void logStartTestSuite(TestSuite testSuite) throws Exception; 
  public void logEndTestSuite(TestSuite testSuite) throws Exception; 
  
  public void logStartTest(TestCase testCase) throws Exception;  
  public void logEndTest(TestCase testCase) throws Exception;

  public void logAction(TestAction action, String status, long time) throws Exception;  
  
  public void logMessage(String message) throws Exception;
  public void logMessage(String[] messages) throws Exception;
  
  public void storeToTxt(String text, String filename) throws Exception;
  
  public void shutDown();
}
