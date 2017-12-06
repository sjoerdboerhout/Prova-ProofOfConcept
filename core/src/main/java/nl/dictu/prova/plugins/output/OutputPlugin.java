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
package nl.dictu.prova.plugins.output;

import nl.dictu.prova.TestRunner;
import nl.dictu.prova.framework.TestCase;

/**
 * Describes the functions that must be available for the other parts of the 
 * framework to execute test actions.
 * 
 * @author  Sjoerd Boerhout
 * @since   0.0.1
 */
public interface OutputPlugin
{
  public String getName();
  
  public void init(TestRunner testRunner) throws Exception;
  
  public void shutDown();
  
  public void setUp(TestCase testCase) throws Exception;
}
