package nl.dictu.prova.framework.web;

import nl.dictu.prova.framework.TestAction;
import nl.dictu.prova.framework.parameters.Bool;
import nl.dictu.prova.framework.parameters.Text;
import nl.dictu.prova.framework.parameters.Xpath;

/**
 * Handles the Prova function 'set text' to set the text of an element on a
 * web page.
 * 
 * @author  Sjoerd Boerhout
 * @since   0.0.1
 */
public class SwitchFrame extends TestAction
{
  // Action attribute names
  public final static String ATTR_XPATH = "XPATH";
  public final static String ATTR_ALERT = "ALERT";
  public final static String ATTR_ACCEPT = "ACCEPT";
   
  // Declaration and default value
  private Xpath xPath;
  private Bool alert;
  private Bool accept;

  /**
   * Constructor
   * @throws Exception 
   */
  public SwitchFrame() throws Exception
  {
    super();
    
    // Create parameters with (optional) defaults and limits
    xPath = new Xpath();
    xPath.setValue("DEFAULT");
    alert = new Bool(false);
    accept = new Bool(false);

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
      case ATTR_XPATH:  
    	  if(value!=null) xPath.setValue(value); 
      break;
      
      case ATTR_ALERT:  
    	  if(value!=null) alert.setValue(value); 
      break;
      case ATTR_ACCEPT:  
    	  if(value!=null) accept.setValue(value); 
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
    if(!xPath.isValid())    return false;
    if(!alert.isValid())   return false;
    if(!accept.isValid())   return false;
    
    return true;
  }
  

  /**
   * Execute this action in the active output plug-in
   */
  @Override
  public void execute() throws Exception
  {
    LOGGER.trace("> Execute test action: {}", () -> this.getClass().getSimpleName());
    
    if(!isValid())
      throw new Exception("Action is not validated!");
    
    testRunner.getWebActionPlugin().doSwitchFrame(xPath.getValue(), alert.getValue(),accept.getValue());
  }


  /**
   * Return a string representation of the objects content
   * 
   * @return 
   */
  @Override
  public String toString()
  {
    return("'" + this.getClass().getSimpleName().toUpperCase() + "': Switch to frame '" + xPath.getValue() + "'");
  }
}
