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
import nl.dictu.prova.framework.parameters.Xpath;
import nl.dictu.prova.framework.parameters.Number;

/**
 * Handles the Prova function 'click' to click on an element on a web page.
 * 
 * @author Sjoerd Boerhout
 * @since 0.0.1
 */
public class Click extends TestAction {
    // Action attribute names
    public final static String ATTR_XPATH = "XPATH";
    public final static String ATTR_RIGHTCLICK = "RIGHTCLICK";
    public final static String ATTR_NUMBEROFCLICKS = "NUMBEROFCLICKS";
    public final static String ATTR_WAITUNTILPAGELOADED = "WAITUNTILPAGELOADED";
    public final static String ATTR_CONTINUEONNOTFOUND = "CONTINUEONNOTFOUND";

    private Xpath xPath;
    private Bool rightClick;
    private Number numberOfClicks;
    private Bool waitUntilPageLoaded;
    private Bool continueOnNotFound;

    /**
     * Constructor
     * 
     * @throws Exception
     */
    public Click() throws Exception {
        super();

        // Create parameters with (optional) defaults and limits
        xPath = new Xpath();

        rightClick = new Bool(false);

        numberOfClicks = new Number(1);
        numberOfClicks.setMinValue(1);
        numberOfClicks.setMaxValue(3);

        waitUntilPageLoaded = new Bool(true);
        continueOnNotFound = new Bool(false);
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

        case ATTR_RIGHTCLICK:
            rightClick.setValue(value);
            break;

        case ATTR_NUMBEROFCLICKS:
            numberOfClicks.setValue(value);
            break;

        case ATTR_WAITUNTILPAGELOADED:
            waitUntilPageLoaded.setValue(value);
            break;

        case ATTR_CONTINUEONNOTFOUND:
            continueOnNotFound.setValue(value);
            break;
        }

        xPath.setAttribute(key, value);
    }

    /**
     * Check if all requirements are met to execute this action
     */
    @Override
    public boolean isValid() throws Exception {

        if (testRunner == null)
            return false;
        if (!xPath.isValid())
            return false;
        if (!rightClick.isValid())
            return false;
        if (!numberOfClicks.isValid())
            return false;
        if (!waitUntilPageLoaded.isValid())
            return false;
        if (!continueOnNotFound.isValid())
            return false;

        return true;
    }

    /**
     * Execute this action in the active output plug-in
     */
    @Override
    public void execute() throws Exception {
        LOGGER.trace("> Execute test action: {}", () -> this.toString());

        if (!isValid())
            throw new Exception("Action is not validated!");

        if (testRunner.containsKeywords(xPath.getValue())) {
            xPath.setValue(testRunner.replaceKeywords(xPath.getValue()));
        }

        testRunner.getWebActionPlugin().doClick(xPath.getValue(), rightClick.getValue(), waitUntilPageLoaded.getValue(),
                continueOnNotFound.getValue());
    }

    /**
     * Return a string representation of the objects content
     * 
     * @return
     */
    @Override
    public String toString() {
        return ("'" + this.getClass().getSimpleName().toUpperCase() + "': Click on element '" + xPath.getValue()
                + "' with " + numberOfClicks.getValue() + " clicks.");
        // (rightClick.getValue() ? " right" : " left") + " clicks. " +
        // "Wait for page loaded: " + waitUntilPageLoaded.getValue());
    }
}
