package nl.dictu.prova.framework.web;

import nl.dictu.prova.framework.TestAction;
import nl.dictu.prova.framework.parameters.Bool;
import nl.dictu.prova.framework.parameters.Text;
import nl.dictu.prova.framework.parameters.Xpath;

/**
 * Handles the Prova function 'send keys' to simulate one ore more key presses
 * in the webbrowser. For example 'tab' key to navigate through elements.
 * 
 * @author  Sjoerd Boerhout
 * @since   0.0.1
 */
public class SendKeys extends TestAction
{
  // Action attribute names
  public final static String ATTR_KEYS = "KEYS";
  public final static String ATTR_XPATH = "XPATH";
  
  private Text keys;
  private Xpath xPath;

  
  /**
   * Constructor
   * @throws Exception 
   */
  public SendKeys() throws Exception
  {
    super(); 
    keys = new Text();
    keys.setMinLength(1);
    xPath = new Xpath();
    xPath.setValue("/html/body");
  }
  

  /**
   * Set attribute <key> with <value>
   * - Unknown attributes are ignored
   * - Invalid values result in an exception
   * 
   * @param key
   * @param value
   * @throws Exception
   */
  @Override
  public void setAttribute(String key, String value) throws Exception
  {
    LOGGER.trace("Request to set '{}' to '{}'", () -> key, () -> value);
    
    switch(key.toUpperCase())
    {
      case ATTR_PARAMETER:
      case ATTR_KEYS:  
        keys.setValue(value); 
      break;
      case ATTR_XPATH:  
    	  if(value!=null) xPath.setValue(value); 
        break;
    } 
    xPath.setAttribute(key, value);
  }
  

  /**
   * Check if all requirements are met to execute this action
   */
  @Override
  public boolean isValid()
  {
    if(testRunner == null)  return false;
    if(!keys.isValid())     return false;
    if(!xPath.isValid())     return false;
    
    return true;
  }


  /**
   * Execute this action in the active output plug-in
   */
  @Override
  public void execute() throws Exception
  {
    LOGGER.trace("> Execute test action: {}", () -> this.toString());
    
    if(!isValid())
      throw new Exception("Action is not validated!");
    
    testRunner.getWebActionPlugin().doSendKeys(xPath.getValue(), keys.getValue());
  }


  /**
   * Return a string representation of the objects content
   * 
   * @return 
   */
  @Override
  public String toString()
  {
    return("'" + this.getClass().getSimpleName().toUpperCase() + "': Send keys '" + keys.getValue() + "' to browser.");
  }
}
