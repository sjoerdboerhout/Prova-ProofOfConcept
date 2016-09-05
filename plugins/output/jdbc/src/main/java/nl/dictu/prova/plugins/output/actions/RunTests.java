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
package nl.dictu.prova.plugins.output.actions;

import nl.dictu.prova.framework.TestAction;
import nl.dictu.prova.framework.TestStatus;
import nl.dictu.prova.plugins.output.JDBC;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Coos van der Galiën
 */
public class RunTests extends TestAction
{
  protected final static Logger LOGGER = LogManager.getLogger(RunTests.class.
          getName());
  
  JDBC jdbc;
  
  public RunTests(JDBC jdbc)
  {
    super(LOGGER);
    
    this.jdbc = jdbc;
  }

  @Override
  public TestStatus execute()
  {
    LOGGER.trace("Executing test for property '" + property + "' with validation '" + test + "'");

    
      if(test.equalsIgnoreCase("{null}")){
          if(jdbc.getTestCase().getVariable(property) != null | testRunner.getPropertyValue(property).trim().length() > 0){
              LOGGER.info("Test unsuccesful!");
              return false;
          } else {
              LOGGER.info("Test succesful!");
              return true;
          }
      }

      if(testRunner.hasPropertyValue(property) | testRunner.getPropertyValue(property) != null | testRunner.getPropertyValue(property).trim().length() > 0){
          String propertyValue = testRunner.getPropertyValue(property).trim();
          if(propertyValue.equalsIgnoreCase(test.trim())){
              LOGGER.info("Test succesful!");
              return true;
          }
          LOGGER.info("Test unsuccesful!");
          return false;
      } else {
          LOGGER.info("Test unsuccesful!");
          return false;
      }
  }

  @Override
  public boolean isValid()
  {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public String toString()
  {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
  
}
