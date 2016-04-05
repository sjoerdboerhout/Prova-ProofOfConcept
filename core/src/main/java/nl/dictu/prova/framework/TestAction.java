package nl.dictu.prova.framework;

import nl.dictu.prova.plugins.output.OutputAction;

/**
 * Contains all the common functions of a test action.
 * 
 * @author  Sjoerd Boerhout
 * @since   0.0.1
 */
public abstract class TestAction
{
  protected OutputAction outputAction;
 
  public abstract void execute();
  
  public TestAction()
  {
    // TODO: Implement constructor
  }
}
