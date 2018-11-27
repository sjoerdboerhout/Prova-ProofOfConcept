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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import nl.dictu.prova.Config;
import nl.dictu.prova.TestRunner;
import nl.dictu.prova.framework.exceptions.SetUpActionException;
import nl.dictu.prova.framework.exceptions.TearDownActionException;
import nl.dictu.prova.framework.exceptions.TestActionException;
import nl.dictu.prova.plugins.reporting.ReportingPlugin;

/**
 * Contains all the data of a test case including a list of all actions that
 * are part of this test.
 * 
 * @author  Sjoerd Boerhout
 * @since   2016-04-14
 */
public class TestCase {
  final static Logger LOGGER = LogManager.getLogger();
  
  // Unique test case ID for identification
  private String     id         = "";
  private TestStatus status     = TestStatus.NOTRUN;
  private String     summary    = "";
  private TestRunner testRunner = null;
  private Boolean    error      = false;

  
  private String projectName  = "";
  
  // Test case information
  private String issueId  = "";
  private String priority = "";
  
	//Labels for this testcase. This allows for filtering on testcases using a label 
	private List<String> labels = new ArrayList<String>();

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
	public TestCase(String id) throws Exception {
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
   * Set the test runner reference
   * 
   * @param testRunner
   * @throws Exception
   */
  public void setTestRunner(TestRunner testRunner)
  {
    LOGGER.trace("Set TestRunner reference");
    LOGGER.trace("TC: Testrunner set: {}", (testRunner != null ? "Yes" : "No"));
            
    this.testRunner = testRunner;
  }
  
  /**
   * Get the testRunner of this test case
   * 
   * @return
   */
  public TestRunner getTestRunner()
  {
    return this.testRunner;
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
   * @param summary
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
   * @param setUpAction the testActions to set
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
   * @param tearDownAction the testActions to set
   */
  public void addTearDownAction(TestAction tearDownAction)
  {
    LOGGER.debug("Add teardown action {}", () -> tearDownAction.toString());
    tearDownActions.add(tearDownAction);
  }

	protected void executeAction(TestAction testAction, long waitTime) throws Exception {
		LOGGER.trace("Execute test action: {}", () -> testAction.toString());
        //error = false;
		try {
			long start = System.nanoTime();
			testAction.execute();
			long end = System.nanoTime();
			long executionTime = (end - start) / 1000000;

			for (ReportingPlugin reportPlugin : testRunner.getReportingPlugins()) {
				LOGGER.debug("Report: logging action with status OK and executiontime {}ms", executionTime);
				reportPlugin.logAction(testAction, "OK", executionTime);
			}
		} catch (Exception eX) {
			for (ReportingPlugin reportPlugin : testRunner.getReportingPlugins()) {
				LOGGER.debug("Report: logging action with status NOK");
				reportPlugin.logAction(testAction, "NOK", 0);
			}
			if (this.testRunner.getPropertyValue("prova.flow.failon.actionfail").equalsIgnoreCase("true")) {
                if(this.testRunner.getPropertyValue("prova.flow.failon.testfail").equalsIgnoreCase("false")&&eX.getMessage().contains("Validation Failed:"))
                {
                    LOGGER.debug("Validation failed");
                    error = true;
                }
                else
                {
                    error = false;
                    throw eX;
                }
            }
            else
            {
                if(this.testRunner.getPropertyValue("prova.flow.failon.testfail").equalsIgnoreCase("false")&&eX.getMessage().contains("Validation Failed:"))
                {
                    LOGGER.debug("Validation failed");
                    error = true;
                }
                else
                {
                    LOGGER.trace("Action failed; continueing due 'actionfail' property is set to false");
                    error = true;
                    //LOGGER.error("Error: " + eX.getMessage());
                    //throw eX;
                }

			}
		}
		try {
			LOGGER.trace("Wait {} ms before executing next action.", waitTime);
			Thread.sleep(waitTime);
		} catch (Exception eX) {
			LOGGER.debug("Exception while waiting '{}' ms: {}", 2000, eX.getMessage());

			throw eX;
		}

	}
  
  /**
   * Run this test case by executing all it's actions.
   * 
   * @throws Exception 
   */
  public void execute() throws  Exception
  {
    Exception exception = null;
    Long waitTime = (long) 0;
    
    LOGGER.info("Execute TC: '{}'", this.toString());
    try
    {
      LOGGER.trace("Configured delay time: '{}'ms", testRunner.getPropertyValue(Config.PROVA_TESTS_DELAY));
      waitTime = Long.parseLong(testRunner.getPropertyValue(Config.PROVA_TESTS_DELAY));
    }
    catch(Exception eX)
    {
      LOGGER.warn("Invalid test delay time. Falling back to default: 50 ms ({})", eX.getMessage());
      waitTime = (long) 50;
    }
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
      eX.printStackTrace();
    }
    
    // Execute all test actions if set up succeeded
		if (exception == null) {
				if (!error)
                    this.setStatus(TestStatus.PASSED);
            
				//TestAction currentTestAction = null;
				try {
					for (TestAction testAction : getTestActions()) {
						//currentTestAction = testAction;
                        if (testAction.getCondition() != null) {
                            try {
                                String condition = testAction.getCondition();
                                LOGGER.trace("Found condition on testaction: " + condition);
                                if (testRunner.containsKeywords(condition)) {
                                    condition = testRunner.replaceKeywords(condition);
                                }
                                String[] conditionParts;
                                if (condition.contains("!=")) {
                                    LOGGER.trace("Found != in the condition... going to split");
                                    conditionParts = condition.split("!=");
                                    //LOGGER.trace(conditionParts[0].trim() +" <-> "+conditionParts[1].trim());
                                    if (!conditionParts[0].trim().equalsIgnoreCase(conditionParts[1].trim())) {

                                        executeAction(testAction, waitTime);
                                    }
                                    else {
                                        LOGGER.trace("Skipping " + testAction.getId() + " based on condition: " + condition );
                                    }
                                } else {
                                    LOGGER.trace("condition should contain = ... going to split");
                                    conditionParts = condition.split("=");
                                    //LOGGER.trace(conditionParts[0].trim() +" <-> "+conditionParts[1].trim());
                                    if (conditionParts[0].trim().equalsIgnoreCase(conditionParts[1].trim())) {

                                        executeAction(testAction, waitTime);
                                    }
                                    else {
                                        LOGGER.trace("Skipping " + testAction.getId() + " based on condition: " + condition );
                                    }
                                }
                            }
                            catch(Exception ex){
                                LOGGER.trace("Evaluating condition failed");
                                throw ex;
                            }


                        }
						else {
                            executeAction(testAction, waitTime);
						}

						//LOGGER.debug(testAction.getId().toString());
					}
                    if (error)
                    {
                        this.setStatus(TestStatus.COMPLETED);
                    }
				} 
				catch (Exception eX) 
				{
			        if (error)
			        {
			            this.setStatus(TestStatus.COMPLETED);
			        }
			        else
			        {
			        	this.setStatus(TestStatus.FAILED);
						this.setSummary(eX.getMessage());
						eX.printStackTrace();
						exception = new TestActionException(eX.getMessage());
			        }  

					
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
			LOGGER.error(eX);
    }
    
    // Exception occured? Throw it back to the test suite
    if(exception != null) 
      throw exception;
	}

	public LinkedList<TestAction> getTestActions() {
		return testActions;
  }
  
  /**
   * Clear all test cases when the test case is executed.
   * This to minimize memory usage.
   */
  public void clearAllActions()
  {
    try
    {
      setUpActions.clear();
      testActions.clear();
      tearDownActions.clear();
    }
    catch(Exception eX)
    {
      LOGGER.warn("Exception whie clearing test actions for TC '{}'", this.getId(), eX);
    }
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
	public List<String> getLabels() {
		return labels;
	}
	
	/**
	 * Determines is this testcase has the label given.
	 * 
	 * @param label
	 * @return
	 */
	public boolean hasLabel(String label) {
		return labels.contains(label);
	}

	/**
	 * Fill label list using commaseparated string
	 * @param labelString
	 */
	public void setLabels(String labelString) {
		this.labels = Arrays.asList(labelString.split(","));
	}
}