package nl.dictu.prova.plugins.input;

import nl.dictu.prova.TestRunner;
import nl.dictu.prova.framework.TestCase;

/**
 * Describes the functions that must be available for the test runner
 * 
 * @author  Sjoerd Boerhout
 * @since   0.0.1
 */
public interface InputPlugin
{
  public void init(TestRunner testRunner) throws Exception;
  
  public void setTestRoot(String testRoot, String projectName) throws Exception;
  public void setLabels(String[] labels);
  public void setTestCaseFilter(String[] testCases);
  
  public void setUp() throws Exception;
  
  public void loadTestCase(TestCase testCase) throws Exception;
  
  public void shutDown();
  
  public String getName();
}
