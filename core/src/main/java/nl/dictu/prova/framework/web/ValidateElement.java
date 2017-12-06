/**
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * <p>
 * http://ec.europa.eu/idabc/eupl
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * <p>
 * Date:      18-12-2016
 * Author(s): Sjoerd Boerhout, Robert Bralts & Coos van der Galiën
 * <p>
 */
package nl.dictu.prova.framework.web;

import nl.dictu.prova.framework.TestAction;
import nl.dictu.prova.framework.parameters.Bool;
import nl.dictu.prova.framework.parameters.TimeOut;
import nl.dictu.prova.framework.parameters.Xpath;

/**
 * Handles the Prova function 'validate element' to check if the given element
 * is (not) available on the web page.
 * 
 * @author  Sjoerd Boerhout
 * @since   0.0.1
 */
public class ValidateElement extends TestAction
{
  // Action attribute names
  public final static String ATTR_XPATH    = "XPATH";
  public final static String ATTR_EXISTS   = "EXISTS";
  public final static String ATTR_TIMEOUT  = "TIMEOUT";
   
  private Xpath   xPath;
  private Bool    exists;
  private TimeOut timeOut;

  
  /**
   * Constructor
   * @throws Exception 
   */
  public ValidateElement() throws Exception
  {
    super();
 
    // Create parameters with (optional) defaults and limits
    xPath = new Xpath();
    exists = new Bool(true);
    timeOut = new TimeOut();
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
      case ATTR_EXISTS:
        exists.setValue(value); 
      break;
      
      case ATTR_TIMEOUT:
        timeOut.setValue(value); 
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
    if(!exists.isValid())   return false;
    if(!timeOut.isValid())  return false;
    
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
    
    testRunner.getWebActionPlugin().doValidateElement(xPath.getValue(), exists.getValue(), timeOut.getValue());
  }


  /**
   * Return a string representation of the objects content
   * 
   * @return 
   */
  @Override
  public String toString()
  {
    return("'" + this.getClass().getSimpleName().toUpperCase() + "': Validate that element '" + xPath.getValue() + "' " +
           (exists.getValue() ? "does" : "doesn't ") + "exist. " +
           "TimeOut: " + timeOut.getValue());
  }
}
