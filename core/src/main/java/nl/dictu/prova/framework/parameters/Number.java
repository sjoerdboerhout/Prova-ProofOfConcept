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
 * Contains all the common functions of the test action parameter Number.
 * 
 * @author Sjoerd Boerhout
 * @since 2016-04-21
 */
public class Number extends Parameter {
    protected Integer value = 0;

    private Integer MIN_VALUE = Integer.MIN_VALUE;
    private Integer MAX_VALUE = Integer.MAX_VALUE;

    private Integer minValue = MIN_VALUE;
    private Integer maxValue = MAX_VALUE;

    /**
     * Constructor
     */
    public Number() {
        super();
    }

    /**
     * Constructor with default value
     * 
     * @throws Exception
     */
    public Number(Integer value) throws Exception {
        super();
        setValue(value);
    }

    /**
     * Set value.
     * 
     * @param value
     * @throws Exception
     */
    public void setValue(Integer value) throws Exception {
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
        // Parse string as integer. Don't accept Null as input
        setValue(parseInteger(value, false));
    }

    /**
     * Get configured value.
     * 
     * @return
     */
    public Integer getValue() {
        return this.value;
    }

    /**
     * Validate if configured value is a valid Number.
     * 
     * @param number
     * @return
     */
    public boolean isValid() {
        return isValid(value);
    }

    /**
     * Validate if number is a valid Number.
     * 
     * @param number
     * @return
     */
    public boolean isValid(Integer value) {
        LOGGER.trace("Validate '{}' as Number.", () -> value);

        // TODO implement a proper timeOut validation
        return validateInteger(value, minValue, maxValue);
    }

    /**
     * Set min value.
     * 
     * @param minValue
     * @throws Exception
     */
    public void setMinValue(Integer minValue) throws Exception {
        if (minValue < MIN_VALUE) {
            throw new Exception("Minimum value must be >= " + MIN_VALUE + " (" + minValue + ")");
        }

        if (minValue > maxValue) {
            throw new Exception("New maximum timeout must be <= " + maxValue + " (" + minValue + ")");
        }

        this.minValue = minValue;
    }

    /**
     * Set max value.
     * 
     * @param maxValue
     * @throws Exception
     */
    public void setMaxValue(Integer maxValue) throws Exception {
        if (maxValue > MAX_VALUE) {
            throw new Exception("Maximum value must be <= " + MAX_VALUE + " (" + maxValue + ")");
        }

        if (maxValue < minValue) {
            throw new Exception("New maximum value must be >= " + minValue + " (" + maxValue + ")");
        }

        this.maxValue = maxValue;
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
