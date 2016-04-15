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
    assertTrue(TestStatus.values().length == 4);
  }
  
  @Test
  public void checkValuesOfEnums()
  {
    assertTrue(TestStatus.NOTRUN.getValue().equals("NotRun"));
    assertTrue(TestStatus.BLOCKED.getValue().equals("Blocked"));
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
