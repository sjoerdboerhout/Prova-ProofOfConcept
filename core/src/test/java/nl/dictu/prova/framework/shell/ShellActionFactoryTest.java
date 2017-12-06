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
package nl.dictu.prova.framework.shell;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.BeforeClass;
import org.junit.Test;

import nl.dictu.prova.Junit;
import nl.dictu.prova.framework.TestAction;

public class ShellActionFactoryTest {
    /*
     * One-time initialization code
     */
    @BeforeClass
    public static void oneTimeSetUp() {
        Junit.configure();
    }

    /*
     * Issue ID: PROVA-38 Requirement: A factory produces specific actions with a common interface
     * 
     * Test if the action word gives the correct action
     */
    @Test
    public void TestExecute() {
        try {
            TestAction testAction = new ShellActionFactory().getAction("Execute");
            assertTrue(testAction instanceof Execute);

            testAction = new ShellActionFactory().getAction("execute");
            assertTrue(testAction instanceof Execute);

            testAction = new ShellActionFactory().getAction("EXECUTE");
            assertTrue(testAction instanceof Execute);
        } catch (Exception eX) {
            fail(eX.getMessage());
        }
    }
}
