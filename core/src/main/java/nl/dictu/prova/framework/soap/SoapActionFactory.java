package nl.dictu.prova.framework.soap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import nl.dictu.prova.framework.TestAction;

/**
 * A factory that allows the input plug-in to create a new web-action.
 * 
 * @author  Coos van der Galiën
 * @since   2016-06-27
 */
public class SoapActionFactory {

  protected final static Logger LOGGER = LogManager.getLogger();
  
  public final static String ACTION_SETMESSAGE      = "SETMESSAGE";
  public final static String ACTION_SETPROPERTIES   = "SETPROPERTIES";
  public final static String ACTION_SETTESTS        = "SETTESTS";
  public final static String ACTION_PROCESSRESPONSE = "PROCESSRESPONSE";
    
  /**
   * Get the corresponding action for <name>
   * 
   * @param name
   * @return
   * @throws Exception
   */
  public static TestAction getAction(String name) throws Exception
  {
    LOGGER.trace("Request to produce webaction '{}'", () -> name);
    
    switch(name.toUpperCase())
    {
      case ACTION_SETMESSAGE:      return new SetMessage();
      case ACTION_SETPROPERTIES:   return new SetProperties();
      case ACTION_SETTESTS:        return new SetTests();
      case ACTION_PROCESSRESPONSE: return new ProcessResponse();
    }
    
    throw new Exception("Unknown action '" + name + "' requested");
  }
}
