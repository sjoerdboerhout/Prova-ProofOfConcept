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
 * Date:      23-08-2016
 * Author(s): Sjoerd Boerhout
 * <p>
 */
package nl.dictu.prova.plugins.output.selenium.actions;

import java.util.logging.Level;
import nl.dictu.prova.framework.TestAction;
import nl.dictu.prova.framework.TestStatus;
import nl.dictu.prova.framework.parameters.Bool;
import nl.dictu.prova.framework.parameters.Xpath;
import nl.dictu.prova.plugins.output.selenium.Selenium;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebElement;

/**
 *
 * @author Sjoerd Boerhout
 */
public class Select extends TestAction
{
  Selenium selenium;
  Xpath xPath;
  Bool select;

  public Select(Selenium selenium)
  {
    this.selenium = selenium;
    
    xPath = new Xpath();
    try
    {
      select = new Bool(true);
    }
    catch (Exception ex)
    {
    }
  }

  
  /**
   * Execute this action
   */ 
  @Override
  public TestStatus execute()
  {
    LOGGER.debug(">> {}Select '{}'", (select.getValue() ? "" : "De-"), xPath.getValue());
    
    int count = 0;
    
    if(!isValid())
    {
      LOGGER.error("Action is not validated!");
      return TestStatus.FAILED;
    }
    
    while(true)
    {
      try
      {
        WebElement element = selenium.findElement(xPath.getValue());
        
        if(element == null)
          throw new Exception("Element '" + xPath + "' not found.");
      
        // Check if current and desired state are not equal
        if(select.getValue() != element.isSelected())
          element.click();
        
        // Action succeeded. Return.
        return TestStatus.PASSED;
      }
      catch(Exception eX)
      {
        if(++count > selenium.getMaxRetries())
        {
          LOGGER.debug("Exception while selecting '{}': {} (retry count: {})", xPath.getValue(), eX.getMessage(), count);
          return TestStatus.FAILED;
        }
      }
    }
  }


  /**
   * Return a string representation of the objects content
   * 
   * @return 
   */
  @Override
  public String toString()
  {
    return("'" + this.getClass().getSimpleName().toUpperCase() + "': " + (select.getValue() ? "Select" : "Deselect") + " '" + xPath.getValue() + "'");
  }
  
  
  /**
  * Check if all requirements are met to execute this action
  */
  @Override
  public boolean isValid()
  {
    if(selenium == null)    return false;
    if(!xPath.isValid())    return false;
    if(!select.isValid())   return false;
    
    return true;
  }
}
