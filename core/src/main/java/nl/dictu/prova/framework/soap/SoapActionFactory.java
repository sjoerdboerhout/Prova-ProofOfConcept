package nl.dictu.prova.framework.soap;

import nl.dictu.prova.framework.TestAction;
import nl.dictu.prova.framework.ActionFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A factory that allows the input plug-in to create a new web-action.
 * 
 * @author  Coos van der Galiën
 * @since   2016-06-27
 */
public class SoapActionFactory implements ActionFactory {

  protected final static Logger LOGGER = LogManager.getLogger();
  
  public final static String ACTION_SETMESSAGE      = "SETSOAPQUERY";
  public final static String ACTION_SETPROPERTIES   = "SETSOAPPROPERTIES";
  public final static String ACTION_EXECUTETEST     = "EXECUTESOAPTEST";
  public final static String ACTION_PROCESSRESPONSE = "PROCESSSOAPRESPONSE";
    
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
      case ACTION_SETMESSAGE:      return new SetSoapMessage();
      case ACTION_SETPROPERTIES:   return new SetSoapProperties();
      case ACTION_EXECUTETEST:     return new ExecuteSoapTest();
      case ACTION_PROCESSRESPONSE: return new ProcessSoapResponse();
    }
    
    throw new Exception("Unknown action '" + name + "' requested");
  }
}
