package nl.dictu.prova.plugins.output;

import nl.dictu.prova.TestRunner;

/**
 * Describes the functions that must be available for the other parts of the 
 * framework to execute test actions.
 * 
 * @author  Sjoerd Boerhout
 * @since   0.0.1
 */
public interface OutputPlugin
{
  public void init(TestRunner testRunner);
  public void setUp() throws Exception;
  public void shutDown();
  
  public void getAction(String action) throws Exception;
}
