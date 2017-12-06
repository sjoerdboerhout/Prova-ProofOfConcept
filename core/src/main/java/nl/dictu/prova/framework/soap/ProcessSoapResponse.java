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
package nl.dictu.prova.framework.soap;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import nl.dictu.prova.framework.TestAction;

public class ProcessSoapResponse extends TestAction {

    String currentPrefix;

    @Override
    public void setAttribute(String key, String value) throws Exception {

    }

    @Override
    public void execute() throws Exception {
        if (!isValid()) {
            throw new Exception("testRunner not properly set!");
        }

        // Check if prefix is provided. Set provided or default prefix to be used with property storage.
        currentPrefix = this.testRunner.getSoapActionPlugin().doGetCurrentPrefix();

        // Add all returning properties to global collection
        Set<Map.Entry<Object, Object>> map = this.testRunner.getSoapActionPlugin().doProcessResponse().entrySet();
        if (!map.isEmpty()) {
            for (Entry entry : map) {
                this.testRunner.setPropertyValue(currentPrefix + "_" + (String) entry.getKey(),
                        (String) entry.getValue());
                // LOGGER.trace("Added key " + (String) entry.getKey() + " and value " + (String) entry.getValue() + "
                // to properties");
            }
        }
    }

    @Override
    public boolean isValid() throws Exception {
        if (testRunner == null)
            return false;
        return true;
    }

    /**
     * Return a string representation of the objects content
     *
     * @return
     */
    @Override
    public String toString() {
        return ("'" + this.getClass().getSimpleName().toUpperCase() + "' with prefix '" + currentPrefix + "'");
    }

}
