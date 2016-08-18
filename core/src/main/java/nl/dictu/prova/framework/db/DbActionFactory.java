package nl.dictu.prova.framework.db;

import nl.dictu.prova.framework.ActionFactory;
import nl.dictu.prova.framework.db.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import nl.dictu.prova.framework.TestAction;

/**
 * A factory that allows the input plug-in to create a new web-action.
 * 
 * @author  Sjoerd Boerhout
 * @since   2016-04-19
 */
public class DbActionFactory implements ActionFactory
{
  protected final static Logger LOGGER = LogManager.getLogger();
  
  public final static String ACTION_PROCESSDBRESPONSE   = "PROCESSDBRESPONSE";
  public final static String ACTION_SETDBPROPERTIES     = "SETDBPROPERTIES";
  public final static String ACTION_SETQUERY            = "SETQUERY";
  public final static String ACTION_EXECUTEDBTEST       = "EXECUTEDBTEST";
    
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
      case ACTION_PROCESSDBRESPONSE:    return new ProcessDbResponse();
      case ACTION_SETDBPROPERTIES:      return new SetDbProperties();
      case ACTION_SETQUERY:             return new SetQuery();
      case ACTION_EXECUTEDBTEST:        return new executeDbTest();
    }
    
    throw new Exception("Unknown action '" + name + "' requested");
  }
}
