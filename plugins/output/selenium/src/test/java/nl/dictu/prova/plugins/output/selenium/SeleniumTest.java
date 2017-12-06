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
package nl.dictu.prova.plugins.output.selenium;

import org.junit.*;
import static org.junit.Assert.*;
import java.util.*;

public class SeleniumTest {
    private Collection<String> collection;

    /*
     * One-time initialization code
     */
    @BeforeClass
    public static void oneTimeSetUp() {
        System.out.println("@BeforeClass - oneTimeSetUp");
    }

    /*
     * One-time cleanup code
     */
    @AfterClass
    public static void oneTimeTearDown() {
        System.out.println("@AfterClass - oneTimeTearDown");
    }

    /*
     * Before each test
     */
    @Before
    public void setUp() {
        collection = new ArrayList<String>();
        System.out.println("@Before - setUp");
    }

    /*
     * After each test
     */
    @After
    public void tearDown() {
        collection.clear();
        System.out.println("@After - tearDown");
    }

    /*
     * Issue ID: PROVA-X Requirement: ...
     * 
     * Test description
     */
    @Test
    public void testEmptyCollection() {
        assertTrue(collection.isEmpty());
        System.out.println("@Test - testEmptyCollection");
    }

    /*
     * Issue ID: PROVA-Y Requirement: ...
     * 
     * Test description
     */
    @Test
    public void testOneItemCollection() {
        collection.add("itemA");
        assertEquals(1, collection.size());
        System.out.println("@Test - testOneItemCollection");
    }

    /*
     * Issue ID: PROVA-Z Requirement: ...
     * 
     * Test description
     */
    @Test
    public void testTrue() {
        assertTrue(true);
        System.out.println("@Test - testTrue");
    }
}
