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

import org.junit.Test;

public class TestStatusTest
{
  /*
  NOTRUN("NotRun"),
  BLOCKED("Blocked"),
  PASSED("Passed"),
  FAILED("Failed");
  */
  
  @Test
  public void checkNumberOfEnums()
  {
    assertTrue(TestStatus.values().length == 5);
  }
  
  @Test
  public void checkValuesOfEnums()
  {
    assertTrue(TestStatus.NOTRUN.getValue().equals("NotRun"));
    assertTrue(TestStatus.BLOCKED.getValue().equals("Blocked"));
    assertTrue(TestStatus.COMPLETED.getValue().equals("CompletedWithErrors"));
    assertTrue(TestStatus.PASSED.getValue().equals("Passed"));
    assertTrue(TestStatus.FAILED.getValue().equals("Failed"));
  }
  
  @Test
  public void checkNotRun()
  {    
    assertTrue(TestStatus.lookup("notrun").name().equals("NOTRUN"));
    assertTrue(TestStatus.lookup("NOTRUN").name().equals("NOTRUN"));
    assertTrue(TestStatus.lookup("NotRun").toString().equals("NotRun"));
  }

  @Test
  public void checkBlocked()
  {
    assertTrue(TestStatus.lookup("blocked").name().equals("BLOCKED"));
    assertTrue(TestStatus.lookup("BLOCKED").name().equals("BLOCKED"));
    assertTrue(TestStatus.lookup("Blocked").toString().equals("Blocked"));
  }

  @Test
  public void checkCompleted()
  {
    assertTrue(TestStatus.lookup("completed").name().equals("COMPLETED"));
    assertTrue(TestStatus.lookup("COMPLETED").name().equals("COMPLETED"));
    assertTrue(TestStatus.lookup("Completed").toString().equals("CompletedWithErrors"));
  }
  @Test
  public void checkPassed()
  {    
    assertTrue(TestStatus.lookup("passed").name().equals("PASSED"));
    assertTrue(TestStatus.lookup("PASSED").name().equals("PASSED"));
    assertTrue(TestStatus.lookup("Passed").toString().equals("Passed"));
  }

  @Test
  public void checkFailed()
  {    
    assertTrue(TestStatus.lookup("failed").name().equals("FAILED"));
    assertTrue(TestStatus.lookup("FAILED").name().equals("FAILED"));
    assertTrue(TestStatus.lookup("Failed").toString().equals("Failed"));
  }
}
