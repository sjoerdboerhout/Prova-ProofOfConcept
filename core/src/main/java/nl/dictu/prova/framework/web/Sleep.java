package nl.dictu.prova.framework.web;

import nl.dictu.prova.framework.TestAction;
import nl.dictu.prova.framework.parameters.TimeOut;

/**
 * Handles the Prova function 'sleep' to wait before executing the next action.
 * 
 * @author  Sjoerd Boerhout
 * @since   0.0.1
 */
public class Sleep extends TestAction
{  
  // Action attribute names
  public final static String ATTR_WAITTIME = "WAITTIME";

  private TimeOut waitTime;

  
  /**
   * Constructor
   * @throws Exception 
   */
  public Sleep() throws Exception
  {
    super();
    
    // Create parameters with (optional) defaults and limits
    waitTime = new TimeOut(500);
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
      case ATTR_WAITTIME:
        waitTime.setValue(value); 
      break;
    } 
  }
  

  /**
   * Check if all requirements are met to execute this action
   */
  @Override
  public boolean isValid()
  {
    if(testRunner == null)  return false;
    if(!waitTime.isValid()) return false;
    
    return true;
  }

  
  /**
   * Execute this action in the active output plug-in
   */
  @Override
  public void execute() throws Exception
  {
    LOGGER.trace("Execute test action: {}", () -> this.toString());
    
    if(!isValid())
      throw new Exception("Action is not validated!");
    
    testRunner.getWebActionPlugin().doSleep(waitTime.getValue());
  }


  /**
   * Return a string representation of the objects content
   * 
   * @return 
   */
  @Override
  public String toString()
  {
    return("'" + this.getClass().getSimpleName().toUpperCase() + "': Sleep for '" + waitTime.getValue() + "' Ms");
  }
}
