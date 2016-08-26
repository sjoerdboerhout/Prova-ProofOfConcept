/*
 *  
 *  Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 *  the European Commission - subsequent versions of the EUPL (the "Licence");
 *  You may not use this work except in compliance with the Licence.
 *  You may obtain a copy of the Licence at:
 *  
 *  http://ec.europa.eu/idabc/eupl
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the Licence is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the Licence for the specific language governing permissions and
 *  limitations under the Licence.
 *  
 *  Date:      DD-MM-YYYY
 *  Author(s): <full name author>
 *  
 */
package nl.dictu.prova.plugins.output.selenium.actions;


import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import nl.dictu.prova.framework.TestAction;
import nl.dictu.prova.framework.TestStatus;
import nl.dictu.prova.framework.parameters.Url;
import nl.dictu.prova.plugins.output.selenium.Selenium;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Handles the function 'navigate' to navigate the current browser to an
 * address.
 * 
 * @author Coos van der Galiën
 */
public class Navigate extends TestAction
{
  private Selenium selenium;
  private Url url = null;
  
  public Navigate(Selenium selenium)
  {
    this.selenium = selenium;
    try
    {
      url = new Url();
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
    URL qualifiedUrl = null;
	  LOGGER.debug("Checking url for malformations");
	  
	  try 
    {
      qualifiedUrl = new URL(url.getValue());
                
      LOGGER.debug("Url not malformed, navigating to " + qualifiedUrl.getPath());
	  
      selenium.getWebdriver().navigate().to(qualifiedUrl);  
      return TestStatus.PASSED;
	  } 
    catch (MalformedURLException e) 
    {
	  	LOGGER.debug("Provided URL is malformed.");
      return TestStatus.FAILED;
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
    return ("'" + this.getClass().getSimpleName().toUpperCase()  + "': " + url.getValue() + "'");
  }

  
  /**
  * Check if all requirements are met to execute this action
  */
  @Override
  public boolean isValid()
  {
    if(selenium == null) return false;
    if(!url.isValid()) return false;
    
    return true;
  }
}