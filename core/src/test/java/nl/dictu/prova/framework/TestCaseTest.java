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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.BeforeClass;
import org.junit.Test;

import nl.dictu.prova.Junit;

public class TestCaseTest {
    /*
     * One-time initialization code
     */
    @BeforeClass
    public static void oneTimeSetUp() {
        Junit.configure();
    }

    /*
     * Issue ID: PROVA-32 Requirement: Unique identifier per test case which is not empty
     * 
     * Create a test case with a valid identifier.
     */
    @Test
    public void createTestCaseWithValidIdentifier() {
        try {
            TestCase testCase = new TestCase("qwerty");
            assertTrue(testCase.getId().contentEquals("qwerty"));
        } catch (Exception eX) {
            fail(eX.getMessage());
        }
    }

    /*
     * Issue ID: PROVA-32 Requirement: Unique identifier per test suite which is not empty
     * 
     * Validate that ' ' is not a valid identifier
     */
    @Test
    public void createTestCaseWithEmptyIdentifier() {
        try {
            @SuppressWarnings("unused")
            TestCase tmpTestCase1 = new TestCase(" ");

            fail("Empty identifier is not allowed!");
        } catch (Exception eX) {
        }
    }

    /*
     * Issue ID: PROVA-32 Requirement: Unique identifier per test suite which is not empty
     * 
     * Validate that 'null' is not a valid identifier
     */
    @Test
    public void createTestCaseWithNoIdentifier() {
        try {
            @SuppressWarnings("unused")
            TestCase tmpTestCase1 = new TestCase(null);

            fail("Empty identifier is not allowed!");
        } catch (Exception eX) {
        }
    }

    /*
     * Issue ID: PROVA-X Requirement: Each test has a status Not run, blocked, passed or failed
     * 
     * Check default status not run
     */
    @Test
    public void checkDefaultStatusNotRun() {
        try {
            TestCase testCase = new TestCase("qwerty");
            assertTrue(testCase.getStatus().equals(TestStatus.NOTRUN));
        } catch (Exception eX) {
            fail(eX.getMessage());
        }
    }

    /*
     * Issue ID: PROVA-X Requirement: Each test has a status Not run, blocked, passed or failed
     * 
     * Check all statuses
     */
    @Test
    public void checkAllStatuses() {
        try {
            TestCase testCase = new TestCase("qwerty");

            testCase.setStatus(TestStatus.BLOCKED);
            assertTrue(testCase.getStatus().equals(TestStatus.BLOCKED));

            testCase.setStatus(TestStatus.PASSED);
            assertTrue(testCase.getStatus().equals(TestStatus.PASSED));

            testCase.setStatus(TestStatus.FAILED);
            assertTrue(testCase.getStatus().equals(TestStatus.FAILED));

            testCase.setStatus(TestStatus.NOTRUN);
            assertTrue(testCase.getStatus().equals(TestStatus.NOTRUN));
        } catch (Exception eX) {
            fail(eX.getMessage());
        }
    }

    /*
     * Issue ID: PROVA-X Requirement: Each test has a summary for communicating the current test status
     * 
     * Check default summary is empty
     */
    @Test
    public void checkDefaultSummary() {
        try {
            TestCase testCase = new TestCase("qwerty");
            assertTrue(testCase.getSummary().equals(""));
        } catch (Exception eX) {
            fail(eX.getMessage());
        }
    }

    /*
     * Issue ID: PROVA-X Requirement: Each test has a summary for communicating the current test status
     * 
     * Check invalid summary is replaced with an empty summary
     */
    @Test
    public void checkInvalidSummary() {
        try {
            TestCase testCase = new TestCase("qwerty");
            testCase.setSummary(null);
            assertTrue(testCase.getSummary().equals(""));
        } catch (Exception eX) {
            fail(eX.getMessage());
        }
    }

    /*
     * Issue ID: PROVA-X Requirement: Each test has a summary for communicating the current test status
     * 
     * Check a valid summary
     */
    @Test
    public void checkValidSummary() {
        try {
            TestCase testCase = new TestCase("qwerty");
            testCase.setSummary("azerty");
            assertTrue(testCase.getSummary().equals("azerty"));
        } catch (Exception eX) {
            fail(eX.getMessage());
        }
    }

    /*
     * Issue ID: PROVA-X Requirement: Each test has an issue id as link to a requirement
     * 
     * Check the default issue id is empty
     */
    @Test
    public void checkDefaultIssueId() {
        try {
            TestCase testCase = new TestCase("qwerty");
            assertTrue(testCase.getIssueId().equals(""));
        } catch (Exception eX) {
            fail(eX.getMessage());
        }
    }

    /*
     * Issue ID: PROVA-X Requirement: Each test has an issue id as link to a requirement
     * 
     * Check an empty issue id results in no change
     */
    @Test
    public void checkEmptyIssueId() {
        try {
            TestCase testCase = new TestCase("qwerty");
            testCase.setIssueId("");
            assertTrue(testCase.getIssueId().equals(""));

            testCase.setIssueId("azerty");
            testCase.setIssueId("");
            assertTrue(testCase.getIssueId().equals("azerty"));
        } catch (Exception eX) {
            fail(eX.getMessage());
        }
    }

    /*
     * Issue ID: PROVA-X Requirement: Each test has an issue id as link to a requirement
     * 
     * Check an invalid issue id results in no change
     */
    @Test
    public void checkInvalidIssueId() {
        try {
            TestCase testCase = new TestCase("qwerty");
            testCase.setIssueId(null);
            assertTrue(testCase.getIssueId().equals(""));

            testCase.setIssueId("azerty");
            testCase.setIssueId(null);
            assertTrue(testCase.getIssueId().equals("azerty"));
        } catch (Exception eX) {
            fail(eX.getMessage());
        }
    }

    /*
     * Issue ID: PROVA-X Requirement: Each test has an issue id as link to a requirement
     * 
     * Check a valid issue id results in an exception
     */
    @Test
    public void checkValidIssueId() {
        try {
            TestCase testCase = new TestCase("qwerty");
            testCase.setIssueId("azerty");
            assertTrue(testCase.getIssueId().equals("azerty"));
        } catch (Exception eX) {
            fail(eX.getMessage());
        }
    }

    /*
     * Issue ID: PROVA-X Requirement: Each test has a priority reference
     * 
     * Check the default priority is empty
     */
    @Test
    public void checkDefaultPriority() {
        try {
            TestCase testCase = new TestCase("qwerty");
            assertTrue(testCase.getPriority().equals(""));
        } catch (Exception eX) {
            fail(eX.getMessage());
        }
    }

    /*
     * Issue ID: PROVA-X Requirement: Each test has an priority
     * 
     * Check an empty priority results in no change
     */
    @Test
    public void checkEmptyPriority() {
        try {
            TestCase testCase = new TestCase("qwerty");
            testCase.setPriority("");
            assertTrue(testCase.getPriority().equals(""));

            testCase.setPriority("high");
            testCase.setPriority("");
            assertTrue(testCase.getPriority().equals("high"));
        } catch (Exception eX) {
            fail(eX.getMessage());
        }
    }

    /*
     * Issue ID: PROVA-X Requirement: Each test has an issue id as link to a requirement
     * 
     * Check an invalid priority
     */
    @Test
    public void checkInvalidPriority() {
        try {
            TestCase testCase = new TestCase("qwerty");
            testCase.setPriority(null);
            assertTrue(testCase.getPriority().equals(""));

            testCase.setPriority("high");
            testCase.setPriority(null);
            assertTrue(testCase.getPriority().equals("high"));
        } catch (Exception eX) {
            fail(eX.getMessage());
        }
    }

    /*
     * Issue ID: PROVA-X Requirement: Each test has an issue id as link to a requirement
     * 
     * Check a valid issue id results in an exception
     */
    @Test
    public void checkValidPriority() {
        try {
            TestCase testCase = new TestCase("qwerty");
            testCase.setPriority("high");
            assertTrue(testCase.getPriority().equals("high"));
        } catch (Exception eX) {
            fail(eX.getMessage());
        }
    }

    /*
     * Issue ID: PROVA-X Requirement: Each test has an issue id as link to a requirement
     * 
     * Check a valid issue id results in an exception
     */
    @Test
    public void checkProjectName() {
        try {
            TestCase testCase = new TestCase("qwerty");
            testCase.setProjectName("Sheep");
            assertTrue(testCase.getProjectName().equals("Sheep"));
        } catch (Exception eX) {
            fail(eX.getMessage());
        }
    }
}
