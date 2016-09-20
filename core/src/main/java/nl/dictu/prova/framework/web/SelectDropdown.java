package nl.dictu.prova.framework.web;

import nl.dictu.prova.framework.TestAction;
import nl.dictu.prova.framework.parameters.Text;
import nl.dictu.prova.framework.parameters.Xpath;

/**
 * Handles the Prova function 'selectdropdown' to select an item from a dropdownlist on a web page.
 * 
 * @author  Robert Bralts
 * @since   0.0.1
 */
public class SelectDropdown extends TestAction
{
  // Action attribute names
  public final static String ATTR_XPATH  = "XPATH";
  public final static String ATTR_SELECT = "SELECT";
  
  private Xpath  xPath;
  private Text 	 select;

  
  /**
   * Constructor
   * @throws Exception 
   */
  public SelectDropdown() throws Exception
  {
    super();
    
    // Create parameters with (optional) defaults and limits
    xPath = new Xpath();
    select = new Text();
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
        xPath.setValue(value); 
      break;
      
      case ATTR_PARAMETER:
      case ATTR_SELECT:
        select.setValue(value); 
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
    if(!select.isValid())   return false;
    
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
    
    testRunner.getWebActionPlugin().doSelectDropdown(xPath.getValue(), select.getValue());
  }


  /**
   * Return a string representation of the objects content
   * 
   * @return 
   */
  @Override
  public String toString()
  {
    return("'" + this.getClass().getSimpleName().toUpperCase() + "': " + select.getValue() + " '" + xPath.getValue() + "'");
  }
}