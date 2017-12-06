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
package nl.dictu.prova.framework.parameters;

/**
 * Contains all the common functions of the test action parameter Bool.
 * 
 * @author Sjoerd Boerhout
 * @since 2016-04-21
 */
public class Bool extends Parameter {
    private Boolean value = null;

    /**
     * Constructor with default value False
     */
    public Bool() {
        super();
        this.value = false;
    }

    /**
     * Constructor with configurable default value
     *
     * @param defaultValue
     */
    public Bool(Boolean defaultValue) throws Exception {
        super();

        this.setValue(defaultValue);
    }

    /**
     * Set value.
     * 
     * @param value
     * @throws Exception
     */
    public void setValue(Boolean value) throws Exception {
        if (isValid(value))
            this.value = value;
        else
            throw this.getLastException();
    }

    /**
     * Set value.
     * 
     * @param value
     * @throws Exception
     */
    public void setValue(String value) throws Exception {
        setValue(parseBoolean(value));
    }

    /**
     * Get configured trueFalse value.
     * 
     * @return
     */
    public Boolean getValue() {
        return this.value;
    }

    /**
     * Validate if configured value is a valid Boolean.
     * 
     * @param value
     * @return
     */
    public boolean isValid() {
        return isValid(value);
    }

    /**
     * Validate if <value> is a valid Boolean.
     * 
     * @param value
     * @return
     */
    public boolean isValid(Boolean value) {
        LOGGER.trace("Validate '{}' as a Boolean.", () -> value);

        return validateBoolean(value);
    }

    /**
     * Validate if <value> is a valid Boolean.
     * 
     * @param value
     * @return
     */
    public boolean isValid(String value) {
        try {
            LOGGER.trace("Validate '{}' as a Boolean.", () -> value);

            return validateBoolean(parseBoolean(value));
        } catch (Exception e) {
        }

        return false;
    }

    /**
     * Return a string representation of the objects content
     * 
     * @return
     */
    @Override
    public String toString() {
        return ("'" + this.getClass().getSimpleName().toUpperCase() + "': " + this.value);
    }
}
