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
package nl.dictu.prova.framework.db;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.dictu.prova.framework.TestAction;

/**
 *
 * @author Coos van der Galiën
 */
class ExecuteDbTest extends TestAction {

    String property;
    String test;
    String result = null;

    @Override
    public void setAttribute(String key, String value) throws Exception {
        LOGGER.trace("Request to set test '{}' to '{}'", () -> key, () -> value);
        this.property = key;
        this.test = value;
    }

    @Override
    public void execute() throws Exception {
        LOGGER.info("> Execute test action: {}", () -> this.getClass().getSimpleName());

        if (!isValid()) {
            throw new Exception("testRunner, property or test not properly set!");
        }

        if (testRunner.getDbActionPlugin().doTest(property, test)) {
            result = "succesful";
        } else {
            result = "unsuccesful";
            throw new Exception("Result is unsuccesful");
        }
    }

    @Override
    public boolean isValid() throws Exception {
        if (testRunner == null)
            return false;
        if (property == null)
            return false;
        if (test == null)
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
        if (result == null) {
            return ("'" + this.getClass().getSimpleName().toUpperCase() + "'");
        }
        return ("'" + this.getClass().getSimpleName().toUpperCase() + "': Value of property '" + property
                + "' was checked with validation '" + test + "', result is '" + result + "'");
    }

}
