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

import nl.dictu.prova.framework.TestAction;
import nl.dictu.prova.framework.TestStatus;
import nl.dictu.prova.plugins.output.selenium.Selenium;

/**
 *
 * @author Coos van der Galiën
 */
public class SwitchScreen extends TestAction
{
  Selenium selenium;

  public SwitchScreen(Selenium selenium)
  {
    this.selenium = selenium;
  }

  @Override
  public TestStatus execute()
  {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public String toString()
  {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

}
