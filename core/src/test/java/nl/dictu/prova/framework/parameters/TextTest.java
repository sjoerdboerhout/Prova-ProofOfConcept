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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import nl.dictu.prova.framework.TestAction;
import nl.dictu.prova.framework.TestActionImp;

/**
 * Contains all the common functions of the test action parameter Text.
 * 
 * @author Sjoerd Boerhout
 * @since 2016-04-21
 */
public class TextTest {

    /*
     * Issue ID: PROVA-38 Requirement: ...
     * 
     * Test parsing a string
     * 
     * @Test public void TestparseAValidString() { try { TestAction testAction = new TestActionImp();
     * 
     * String value = "blaat"; String testString = testAction.parseString(value, 5, false);
     * assertEquals(testString,value); } catch (Exception eX) { fail(eX.getMessage()); } }
     * 
     * 
     * Issue ID: PROVA-38 Requirement: ...
     * 
     * Test parsing a string that is too short
     * 
     * @Test public void TestparseAnInValidString() { String value = "sheep";
     * 
     * try { TestAction testAction = new TestActionImp();
     * 
     * @SuppressWarnings("unused") String testString = testAction.parseString(value, 6, false);
     * 
     * fail("String is not at least 6 character long!"); } catch (Exception eX) {
     * assertTrue(eX.getMessage().contains(value)); assertTrue(eX.getMessage().contains("6")); } }
     * 
     * 
     * Issue ID: PROVA-38 Requirement: ...
     * 
     * Test parsing a null string
     * 
     * @Test public void TestparseAnEmptyString() { try { TestAction testAction = new TestActionImp();
     * 
     * String value = ""; String testString = testAction.parseString(value, 0, false); assertEquals(testString,value); }
     * catch (Exception eX) { fail(eX.getMessage()); } }
     * 
     * 
     * Issue ID: PROVA-38 Requirement: ...
     * 
     * Test parsing a string
     * 
     * @Test public void TestparseANullString() { try { TestAction testAction = new TestActionImp();
     * 
     * String testString = testAction.parseString(null, 6, true);
     * 
     * assertTrue(testString == null); } catch (Exception eX) { fail("Null is allowed as value!"); } }
     * 
     * protected String value = null;
     * 
     * private final Integer MIN_LENGTH = 0; private final Integer MAX_LENGTH = Integer.MAX_VALUE;
     * 
     * protected Integer minLength = MIN_LENGTH; protected Integer maxLength = MAX_LENGTH;
     * 
     * 
     *//**
        * Constructor
        */
    /*
     * public Text() { super(); }
     * 
     * 
     *//**
        * Set text.
        * 
        * @param text
        * @throws Exception
        */
    /*
     * public void setValue(String value) throws Exception { if(isValid(value)) this.value = value; else throw
     * this.getLastException(); }
     * 
     * 
     *//**
        * Get configured text.
        * 
        * @return
        */
    /*
     * public String getValue() { return this.value; }
     * 
     * 
     *//**
        * Validate if configured <value> is a valid text.
        * 
        * @param value
        * @return
        */
    /*
     * public boolean isValid() { return isValid(value); }
     * 
     * 
     *//**
        * Validate if <value> is a valid text.
        * 
        * @param value
        * @return
        */
    /*
     * public boolean isValid(String text) { LOGGER.trace("Validate '{}' as a String. Min: {}, Max: {}", () -> text, ()
     * -> minLength, () -> maxLength);
     * 
     * return validateString(text, minLength, maxLength); }
     * 
     * 
     *//**
        * Set min length.
        * 
        * @param minLength
        * @throws Exception
        */
    /*
     * public void setMinLength(Integer minLength) throws Exception { if(minLength < MIN_LENGTH) { throw new
     * Exception("Minimum value must be >= " + MIN_LENGTH + " (" + minLength + ")"); }
     * 
     * if(minLength > maxLength) { throw new Exception("New maximum length must be <= " + maxLength + " (" + minLength +
     * ")"); }
     * 
     * this.minLength = minLength; }
     * 
     * 
     *//**
        * Set max length.
        * 
        * @param maxLength
        * @throws Exception
        *//*
           * public void setMaxLength(Integer maxLength) throws Exception { if(maxLength > MAX_LENGTH) { throw new
           * Exception("Maximum value must be <= " + MAX_LENGTH + " (" + maxLength + ")"); }
           * 
           * if(maxLength < minLength) { throw new Exception("New maximum timeout must be >= " + minLength + " (" +
           * maxLength + ")"); }
           * 
           * this.maxLength = maxLength; }
           */
}
