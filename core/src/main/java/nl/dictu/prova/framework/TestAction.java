package nl.dictu.prova.framework;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import nl.dictu.prova.TestRunner;
import nl.dictu.prova.plugins.output.OutputPlugin;

/**
 * Contains all the common functions of a test action.
 * 
 * @author  Sjoerd Boerhout
 * @since   2016-04-06
 */
public abstract class TestAction
{
  protected final static Logger LOGGER = LogManager.getLogger();
  
  public final static String ATTR_PARAMETER = "PARAMETER";
  
  protected TestRunner   testRunner;
  protected OutputPlugin outputPlugin;
  protected Exception    lastValidationException = null;
 
  public abstract void setAttribute(String key, String value) throws Exception;
  
  public abstract void execute() throws Exception;
  public abstract boolean isValid() throws Exception;
  
  /**
   * Constructor
   */
  public TestAction()
  {
    // TODO: Implement constructor
  }
  
  /**
   * Set a reference to the test runner. Needed to access the output plug-in.
   * 
   * @param testRunner
   */
  public void setTestRunner(TestRunner testRunner)
  {
    this.testRunner = testRunner;
  }
  
  /**
   * Get the last validation exception that occurred
   * @return
   */
  public Exception getLastValidationException()
  {
    return this.lastValidationException;
  }
}
