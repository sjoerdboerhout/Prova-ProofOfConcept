package nl.dictu.prova.framework.shell;

import nl.dictu.prova.framework.ActionFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import nl.dictu.prova.framework.TestAction;

/**
 * A factory that allows the input plug-in to create a new shell-action.
 * 
 * @author  Sjoerd Boerhout
 * @since   2016-04-19
 */
public class ShellActionFactory implements ActionFactory
{
  protected final static Logger LOGGER = LogManager.getLogger();
  
  public final static String ACTION_EXECUTE = "EXECUTE";
  
  /**
   * Get the corresponding action for <name>
   * 
   * @param name
   * @return
   * @throws Exception
   */
  public TestAction getAction(String name) throws Exception
  {
    LOGGER.trace("Request to produce webaction '{}'", () -> name);
    
    switch(name.toUpperCase())
    {
      case ACTION_EXECUTE:  return new Execute();
    }

    throw new Exception("Unknown action '" + name + "' requested");
  }
}
