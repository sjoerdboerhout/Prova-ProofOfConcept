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

import java.io.File;
import java.io.IOException;
import nl.dictu.prova.Config;
import nl.dictu.prova.framework.TestAction;
import nl.dictu.prova.framework.TestStatus;
import nl.dictu.prova.plugins.output.selenium.Selenium;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

/**
 *
 * @author Sjoerd Boerhout
 */
public class CaptureScreen extends TestAction
{
  private Selenium selenium;
  private String fileName;
  
  
  public CaptureScreen(Selenium selenium)
  {
    this.selenium = selenium;
  }
  
  
  /**
   * Execute this action
   */
  @Override
  public TestStatus execute()
  {
    File scrFile = ((TakesScreenshot)selenium.getWebdriver()).getScreenshotAs(OutputType.FILE);
    
    try 
    {
      if(!new File(this.getAttribute("filename")).isFile())
        fileName = this.getAttribute("filename");
      
      if(!isValid())
      {
        LOGGER.error("Action is not validated!");
        return TestStatus.FAILED;
      }
      
      FileUtils.copyFile(scrFile, new File(fileName + "x.png"));
      LOGGER.debug("Placed screen shot in " + fileName + "x.png");
      
      return TestStatus.PASSED;
    } 
    catch (IOException e) 
    {
      LOGGER.error("IOException while taking screenshot!");
      
      e.printStackTrace();
      
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
    return("'" + this.getClass().getSimpleName().toUpperCase() + "': Save a screendump to file '" + fileName + "'");
  }

  
  /**
   * Check if all requirements are met to execute this action
   */
  @Override
  public boolean isValid()
  {
    if(selenium == null) return false;
    if(fileName == null) return false;
    
    return true;
  }

}
