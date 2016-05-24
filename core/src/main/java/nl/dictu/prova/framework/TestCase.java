package nl.dictu.prova.framework;

import java.util.LinkedList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import nl.dictu.prova.framework.exceptions.SetUpActionException;
import nl.dictu.prova.framework.exceptions.TearDownActionException;
import nl.dictu.prova.framework.exceptions.TestActionException;

/**
 * Contains all the data of a test case including a list of all actions that
 * are part of this test.
 * 
 * @author  Sjoerd Boerhout
 * @since   2016-04-14
 */
public class TestCase
{
  final static Logger LOGGER = LogManager.getLogger();
  
  // Unique test case ID for identification
  private String     id       = "";
  private TestStatus status   = TestStatus.NOTRUN;
  private String     summary  = "";
  
  private String projectName  = "";
  
  // Test case information
  private String issueId  = "";
  private String priority = "";

  // Test actions lists per type
  private LinkedList<TestAction> setUpActions     = new LinkedList<TestAction>();
  private LinkedList<TestAction> testActions      = new LinkedList<TestAction>();
  private LinkedList<TestAction> tearDownActions  = new LinkedList<TestAction>();
  
  /**
   * Constructor with mandatory ID
   * 
   * @param id
   * @throws Exception
   */
  public TestCase(String id) throws Exception
  {
    LOGGER.debug("Construct a new TestCase with id '{}'", () -> id);
    
    setId(id);
  }
  
  /**
   * Set the id of this test case
   * 
   * @param id
   * @throws Exception
   */
  private void setId(String id) throws Exception
  {
    LOGGER.trace("Set TestCase id: {}", () -> id);
    
    if(id.trim().length() < 1)
    {
      throw new Exception("Invalid testsuite id: " + id);
    }
    
    this.id = id;
  }
  
  /**
   * Get the id of this test case
   * 
   * @return
   */
  public String getId()
  {
    return this.id;
  }  
    

  /**
   * Update the test status from external source.
   * For example when the test is blocked by another tests failure
   * 
   * @param status
   */
  public void setStatus(TestStatus status)
  {
    LOGGER.debug("Set status of tc '{}' to: {}", () -> this.getId(), () -> status.name());
    
    this.status = status;
  }
  
  /**
   * Get the current status of this test
   * 
   * @return
   */
  public TestStatus getStatus()
  {
    return this.status;
  }  
  

  /**
   * Update the project name for this test script
   * 
   * @param projectName
   */
  public void setProjectName(String projectName)
  {
    LOGGER.debug("Update project name of tc from '{}' to {}", () -> this.projectName, () -> projectName);
    
    this.projectName = projectName;
  }
  
  /**
   * Get the current project name of this test
   * 
   * @return
   */
  public String getProjectName()
  {
    return this.projectName;
  }

  
  /**
   * Set a summary for this test. Usefull when the test could not be
   * completed.
   * 
   * @param testSummary
   */
  public void setSummary(String summary)
  {
    try
    {
      if(summary.trim().length() < 1 || summary == null)
        throw new Exception("Invalid summary '" + summary + "'");
      
      this.summary = summary;
    }
    catch(Exception eX)
    {
      LOGGER.warn("Exception: " + eX + " (" + priority + ")");
    }
  }
  
  /**
   * Get the current summary of the test run result.
   * 
   * @return
   */
  public String getSummary()
  {
    return this.summary;
  }
  
  /**
   * Set the id of the issue this test case will test
   * @param issueId
   */
  public void setIssueId(String issueId)
  {
    LOGGER.trace("New issueId to set {}", () -> this.issueId);
    
    try
    {
      if(issueId.trim().length() < 1 || issueId == null)
        throw new Exception("Invalid priority");
     
      this.issueId = issueId;
    }
    catch(Exception eX)
    {
      LOGGER.warn("Invalid issue id '{}'. Id '{}' not changed.", () -> issueId, () -> this.issueId);
    }
  }
  
  /**
   * Get the issue id of this test case
   * 
   * @return
   */
  public String getIssueId()
  {
    return this.issueId;
  }
  

  /**
   * @param priority the priority to set
   */
  public void setPriority(String priority)
  {
    LOGGER.trace("New priority to set {}", () -> this.priority);
    
    try
    {
      if(priority.trim().length() < 1 || priority == null)
        throw new Exception("Invalid priority '" + priority + "'");
      
      this.priority = priority;
    }
    catch(Exception eX)
    {
      LOGGER.warn("Exception: " + eX + " (" + priority + ")");
    }
  }

  /**
   * @return the priority
   */
  public String getPriority()
  {
    return this.priority;
  }


  /**
   * @param testAction the testActions to set
   */
  public void addSetUpAction(TestAction setUpAction)
  {
    LOGGER.debug("Add setup action '{}'", () -> setUpAction.toString());
    setUpActions.add(setUpAction);
  }

  /**
   * @param testAction the testActions to set
   */
  public void addTestAction(TestAction testAction)
  {
    LOGGER.debug("Add test action {}", () -> testAction.toString());
    testActions.add(testAction);
  }

  /**
   * @param testAction the testActions to set
   */
  public void addTearDownAction(TestAction tearDownAction)
  {
    LOGGER.debug("Add teardown action {}", () -> tearDownAction.toString());
    tearDownActions.add(tearDownAction);
  }

  
  /**
   * Run this test case by executing all it's actions.
   * 
   * @throws Exception 
   */
  public void execute() throws  Exception
  {
    Exception exception = null;
    
    LOGGER.debug("RUN TC: " + this.toString());
    
    // Execute all set up actions
    try
    {
      for(TestAction setUpAction : setUpActions)
      {
        LOGGER.trace("Execute setUp action: {}", () -> setUpAction.toString());
        setUpAction.execute();
      }      
    }
    catch(Exception eX)
    {
      LOGGER.error(eX);
      this.setStatus(TestStatus.FAILED);
      this.setSummary(eX.getMessage());
      exception = new SetUpActionException(eX.getMessage());
    }
    
    // Execute all test actions if set up succeeded
    if(exception == null)
    {
      try
      {
        for(TestAction testAction : testActions)
        {
          LOGGER.trace("Execute test action: {}", () -> testAction.toString());
          testAction.execute();
        }    
        
        // Errors during tearDown do not alter the test result
        this.setStatus(TestStatus.PASSED);
      }
      catch(Exception eX)
      {
        LOGGER.error(eX);
        this.setStatus(TestStatus.FAILED);
        this.setSummary(eX.getMessage());
        exception = new TestActionException(eX.getMessage());
      }
    }
    
    // Always execute the tear down actions
    try
    {
      for(TestAction tearDownAction : tearDownActions)
      {
        LOGGER.trace("Execute tear down action: {}", () -> tearDownAction.toString());
        tearDownAction.execute();
      }    
    }
    catch(TearDownActionException eX)
    {
      LOGGER.error(eX);
      this.setSummary(eX.getMessage());
      if(exception == null)
        exception = eX;      
    }
    catch(Exception eX)
    {
      LOGGER.error(eX);
      this.setSummary(eX.getMessage());
      this.setStatus(TestStatus.FAILED);
      if(exception == null)
        exception = new TearDownActionException(eX.getMessage());
    }
    
    // Exception occured? Throw it back to the test suite
    if(exception != null) 
      throw exception;
  }
  
  /**
   * Summarize this object for logging purpose
   * 
   * @return
   */
  @Override
  public String toString()
  {
    return String.format( "ID: %s (Setup: %d, Actions: %d, Teardown: %d)", 
                          id, 
                          this.setUpActions.size(),
                          this.testActions.size(), 
                          this.tearDownActions.size());
  }  
}