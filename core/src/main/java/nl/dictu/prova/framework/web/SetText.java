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
import nl.dictu.prova.framework.parameters.Text;
import nl.dictu.prova.framework.parameters.Xpath;

/**
 * Handles the Prova function 'set text' to set the text of an element on a web page.
 * 
 * @author Sjoerd Boerhout
 * @since 0.0.1
 */
public class SetText extends TestAction {
    // Action attribute names
    public final static String ATTR_XPATH = "XPATH";
    public final static String ATTR_VALUE = "VALUE";
    public final static String ATTR_REPLACE = "REPLACE";

    // Declaration and default value
    private Xpath xPath;
    private Text text;
    private Bool replace;

    /**
     * Constructor
     * 
     * @throws Exception
     */
    public SetText() throws Exception {
        super();

        // Create parameters with (optional) defaults and limits
        xPath = new Xpath();

        text = new Text();
        text.setMinLength(0);

        replace = new Bool(true);
    }

    /**
     * Set attribute <key> with <value> - Unknown attributes are ignored - Invalid values result in an exception
     * 
     * @param key
     * @param value
     * @throws Exception
     */
    @Override
    public void setAttribute(String key, String value) throws Exception {
        LOGGER.trace("Request to set '{}' to '{}'", () -> key, () -> value);

        switch (key.toUpperCase()) {
        case ATTR_XPATH:
            xPath.setValue(value);
            break;

        case ATTR_PARAMETER:
        case ATTR_VALUE:
            text.setValue(value);
            break;

        case ATTR_REPLACE:
            replace.setValue(value);
            break;
        }

        xPath.setAttribute(key, value);
    }

    /**
     * Check if all requirements are met to execute this action
     */
    @Override
    public boolean isValid() {
        if (testRunner == null)
            return false;
        if (!xPath.isValid())
            return false;
        if (!text.isValid())
            return false;
        if (!replace.isValid())
            return false;

        return true;
    }

    /**
     * Execute this action in the active output plug-in
     */
    @Override
    public void execute() throws Exception {
        LOGGER.trace("> Execute test action: {}", () -> this.getClass().getSimpleName());

        if (!isValid())
            throw new Exception("Action is not validated!");

        if (testRunner.containsKeywords(xPath.getValue())) {
            xPath.setValue(testRunner.replaceKeywords(xPath.getValue()));
        }
        if (testRunner.containsKeywords(text.getValue())) {
            text.setValue(testRunner.replaceKeywords(text.getValue()));
        }
        testRunner.getWebActionPlugin().doSetText(xPath.getValue(), text.getValue(), replace.getValue());
    }

    /**
     * Return a string representation of the objects content
     * 
     * @return
     */
    @Override
    public String toString() {
        return ("'" + this.getClass().getSimpleName().toUpperCase() + "': " + (replace.getValue() ? "Replace" : "Set")
                + " text of '" + xPath.getValue() + "' to '" + text.getValue() + "'");
    }
}
