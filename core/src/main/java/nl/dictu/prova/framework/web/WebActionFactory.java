package nl.dictu.prova.framework.web;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import nl.dictu.prova.framework.TestAction;

/**
 * A factory that allows the input plug-in to create a new web-action.
 * 
 * @author  Sjoerd Boerhout
 * @since   2016-04-19
 */
public class WebActionFactory
{
  protected final static Logger LOGGER = LogManager.getLogger();
  
  public final static String ACTION_CAPTURESCREEN   = "CAPTURESCREEN";
  public final static String ACTION_CLICK           = "CLICK";
  public final static String ACTION_DOWNLOADFILE    = "DOWNLOADFILE";
  public final static String ACTION_SELECT          = "SELECT";
  public final static String ACTION_SENDKEYS        = "SENDKEYS";
  public final static String ACTION_SETTEXT         = "SETTEXT";
  public final static String ACTION_SLEEP           = "SLEEP";
  public final static String ACTION_VALIDATEELEMENT = "VALIDATEELEMENT";
  public final static String ACTION_VALIDATETEXT    = "VALIDATETEXT";
  public final static String ACTION_UPLOADFILE      = "UPLOADFILE";

  
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
      case ACTION_CAPTURESCREEN:   return new CaptureScreen();
      case ACTION_CLICK:           return new Click();
      case ACTION_DOWNLOADFILE:    return new DownloadFile();
      case ACTION_SELECT:          return new Select();
      case ACTION_SENDKEYS:        return new SendKeys();
      case ACTION_SETTEXT:         return new SetText();
      case ACTION_SLEEP:           return new Sleep();
      case ACTION_VALIDATEELEMENT: return new ValidateElement();
      case ACTION_VALIDATETEXT:    return new ValidateText();
      case ACTION_UPLOADFILE:      return new UploadFile();
    }
    
    throw new Exception("Unknown action '" + name + "' requested");
  }
}
