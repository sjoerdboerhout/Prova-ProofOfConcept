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
package nl.dictu.prova.framework;

/**
 * @author Sjoerd Boerhout
 * 
 * @since 2016-04-14
 *
 */
public enum TestStatus {
    NOTRUN("NotRun"), BLOCKED("Blocked"), PASSED("Passed"), FAILED("Failed");

    private String name;

    private TestStatus(String name) {
        this.name = name;
    }

    /**
     * Get the name of this log level
     * 
     * @return
     */
    @Override
    public String toString() {
        return this.name;
    }

    /**
     * Get the name for this test status
     * 
     * @return
     */
    public String getValue() {
        return this.name;
    }

    /**
     * Find enum by it's name
     * 
     * @param name
     * @return
     */
    public static TestStatus lookup(String name) {
        name = name.toUpperCase();

        for (TestStatus testStatus : TestStatus.values()) {
            if (testStatus.name().equalsIgnoreCase(name)) {
                return testStatus;
            }
        }
        return null;
    }
}
