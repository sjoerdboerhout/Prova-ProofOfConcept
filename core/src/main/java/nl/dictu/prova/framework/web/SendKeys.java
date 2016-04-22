package nl.dictu.prova.framework.web;

import nl.dictu.prova.framework.TestAction;
import nl.dictu.prova.framework.parameters.Text;

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
  
  private Text keys;

  
  /**
   * Constructor
   * @throws Exception 
   */
  public SendKeys() throws Exception
  {
    super(); 
    keys = new Text();
    keys.setMinLength(1);
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
    switch(key.toUpperCase())
    {
      case ATTR_PARAMETER:
      case ATTR_KEYS:  
        keys.setValue(value); 
      break;
    } 
  }
  

  /**
   * Check if all requirements are met to execute this action
   */
  @Override
  public boolean isValid()
  {
    if(!keys.isValid())  return false;
    
    return true;
  }


  /**
   * Execute this action in the active output plug-in
   */
  @Override
  public void execute() throws Exception
  {
    // TODO Implement function
    System.out.println("Send keys '" + keys.getValue() + "' to browser.");
  }
}
