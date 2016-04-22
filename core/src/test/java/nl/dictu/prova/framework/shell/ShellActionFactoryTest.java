package nl.dictu.prova.framework.shell;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.BeforeClass;
import org.junit.Test;

import nl.dictu.prova.Junit;
import nl.dictu.prova.framework.TestAction;

public class ShellActionFactoryTest
{
  /*
   *  One-time initialization code
   */
  @BeforeClass 
  public static void oneTimeSetUp()
  {
    Junit.configure();
  }

  /*
   * Issue ID:    PROVA-38
   * Requirement: A factory produces specific actions with a common interface 
   * 
   * Test if the action word gives the correct action
   */
  @Test
  public void TestExecute() 
  {
    try
    {
      TestAction testAction = ShellActionFactory.getAction("Execute");
      assertTrue(testAction instanceof Execute);
      
      testAction = ShellActionFactory.getAction("execute");
      assertTrue(testAction instanceof Execute);
      
      testAction = ShellActionFactory.getAction("EXECUTE");
      assertTrue(testAction instanceof Execute);
    }
    catch (Exception eX)
    {
      fail(eX.getMessage());
    }
  }   
}
