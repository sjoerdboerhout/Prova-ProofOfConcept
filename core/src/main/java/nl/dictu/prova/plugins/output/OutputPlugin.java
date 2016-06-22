package nl.dictu.prova.plugins.output;

import nl.dictu.prova.TestRunner;
import nl.dictu.prova.framework.TestCase;

/**
 * Describes the functions that must be available for the other parts of the 
 * framework to execute test actions.
 * 
 * @author  Sjoerd Boerhout
 * @since   0.0.1
 */
public interface OutputPlugin
{
  public String getName();
  
  public void init(TestRunner testRunner) throws Exception;
  
  public void shutDown();
  
  public void setUp(TestCase testCase) throws Exception;
  
  public void tearDown(TestCase testCase) throws Exception;
}
