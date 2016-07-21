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
  
  public final static String ACTION_PROCESSRESPONSE = "PROCESSRESPONSE";
  public final static String ACTION_SETLOGIN        = "SETLOGIN";
  public final static String ACTION_SENDMESSAGE     = "SENDMESSAGE";
  public final static String ACTION_SETURL	    = "SETURL";
    
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
      case ACTION_PROCESSRESPONSE: return new ProcessResponse();
      case ACTION_SETLOGIN:        return new SetLogin();
      case ACTION_SENDMESSAGE:     return new SendMessage();
      case ACTION_SETURL:	   return new SetUrl();
    }
    
    throw new Exception("Unknown action '" + name + "' requested");
  }
}
