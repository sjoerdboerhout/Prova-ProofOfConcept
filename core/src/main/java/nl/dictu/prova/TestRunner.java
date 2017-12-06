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
package nl.dictu.prova;

import java.util.ArrayList;

import nl.dictu.prova.framework.TestSuite;
import nl.dictu.prova.plugins.input.InputPlugin;
import nl.dictu.prova.plugins.output.DbOutputPlugin;
import nl.dictu.prova.plugins.output.ShellOutputPlugin;
import nl.dictu.prova.plugins.output.WebOutputPlugin;
import nl.dictu.prova.plugins.output.SoapOutputPlugin;
import nl.dictu.prova.plugins.reporting.ReportingPlugin;

/**
 * Describes the functions that must be available for the other parts of the framework to run.
 * 
 * @author Sjoerd Boerhout
 * @since 2016-04-06
 */
public interface TestRunner {
    public Boolean containsKeywords(String input) throws Exception;

    public Boolean hasPropertyValue(String key);

    public String getPropertyValue(String key) throws Exception;

    public void setPropertyValue(String key, String value) throws Exception;

    public String replaceKeywords(String input) throws Exception;

    public void printAllProperties() throws Exception;

    public void setRootTestSuite(TestSuite testSuite);

    public InputPlugin getInputPlugin();

    public WebOutputPlugin getWebActionPlugin();

    public SoapOutputPlugin getSoapActionPlugin();

    public DbOutputPlugin getDbActionPlugin();

    public ShellOutputPlugin getShellActionPlugin();

    public ArrayList<ReportingPlugin> getReportingPlugins();
}
