package nl.dictu.prova.framework;

import nl.dictu.prova.plugins.output.OutputAction;

/**
 * Contains all the common functions of a test action.
 * 
 * @author  Sjoerd Boerhout
 * @since   2016-04-06
 */
public abstract class TestAction
{
  protected OutputAction outputAction;
 
  public abstract void execute() throws Exception;
  
  public TestAction()
  {
    // TODO: Implement constructor
  }
}
