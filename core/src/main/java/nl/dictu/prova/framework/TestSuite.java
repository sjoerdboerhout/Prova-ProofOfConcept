package nl.dictu.prova.framework;

import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import nl.dictu.prova.framework.exceptions.SetUpActionException;
import nl.dictu.prova.framework.exceptions.TearDownActionException;
import nl.dictu.prova.framework.exceptions.TestActionException;

/**
 * Contains a tree of (sub) test suites and test cases.
 * This tree holds all information about test suites and test cases and offers
 * an interface to execute these tests via the execute-function.
 * 
 * @author  Sjoerd Boerhout
 * @since   2016-04-16
 */
public class TestSuite
{
  final static Logger LOGGER = LogManager.getLogger();
  
  private String id = null;
  private TestSuite parent = null;
  private LinkedHashMap<String,TestSuite> testSuites = new LinkedHashMap<String,TestSuite>();
  private LinkedHashMap<String,TestCase> testCases = new LinkedHashMap<String,TestCase>();

  /**
   * Constructor for a test suite without a parent.
   * The id is a unique identifier for the test suite.
   * 
   * @param id
   * @throws Exception
   */
  public TestSuite(String id) throws Exception
  {
    LOGGER.trace("Create new test suite with id '{}'", () -> id);
    
    setId(id);
  }

  /**
   * Constructor for a test suite with a parent.
   * The id is a unique identifier for the test suite.
   * The parent is another test suite.
   * 
   * @param id
   * @param parent
   * @throws Exception
   */
  public TestSuite(String id, TestSuite parent) throws Exception
  {
    LOGGER.trace("Create new test suite with id '{}' and parent {}", () -> id, () -> parent.getId());
    
    setId(id);
    setParent(parent);
  }
  
  /**
   * Set the unique identifier of this test suite. This identifier must be at
   * least 1 character long and is required to be unique in the whole set.
   * 
   * @param testSuiteId
   * @throws Exception
   */
  private void setId(String id) throws Exception
  {
    if(id.trim().length() < 1)
      throw new Exception("Invalid testsuite Id");
    
    this.id = id;
  }
  
  /**
   * Retrieve the unique identifier of this test suite
   * 
   * @return test suite id
   */
  public String getId()
  {
    return id;
  }

  /**
   * Set the parent of this test suite
   * 
   * @param parent
   * @throws Exception
   */
  public void setParent(TestSuite parent) throws Exception
  {
    if(this.hasTestSuite(parent, true))
      throw new Exception("This testsuite already exists (as a child) in this suite!");
    
    this.parent = parent;
  }

  /**
   * Get the parent of this test suite
   * 
   * @return parent test suite
   */
  public TestSuite getParent()
  {
    return parent;
  }

  /**
   * Request if this test suite has a parent
   * 
   * @return true if parent is not null
   */
  public boolean hasParent()
  {
    return parent != null ? true : false;
  }  
  
  /**
   * Get the root of this test suite tree
   * 
   * @return root test suite
   */
  public TestSuite getRootParent()
  {
    TestSuite testSuite = this;
    
    while(testSuite.getParent() != null)
    {
      testSuite = testSuite.getParent();
    }
    
    return testSuite;
  }
 
  
  
  /**
   * Add a new sub test suite to this test suite
   * 
   * @param testSuite
   * @throws Exception
   */
  public void addTestSuite(TestSuite testSuite) throws Exception
  {
    LOGGER.trace("Add child test suite '{}'", () -> testSuite.getId());
    
    // First check if this test suite doesn't exist yet in the structure
    if(!this.hasTestSuite(testSuite.getRootParent(), true))
    {
      testSuites.put(testSuite.getId(), testSuite);
      testSuite.setParent(this);
    }
    else
    {
      throw new Exception("TestSuite " + testSuite.getId() + " is already a member of this testsuite.");
    }
  }

  /**
   * Get the number of child test suites of this test suite
   * 
   * @return
   */
  public int numberOfTestSuites()
  {
    return numberOfTestSuites(false);
  }

  /**
   * Get the number of child test suites of this test suite. When 
   * countSubTestSuites is set to true all sub-suites are counted too.
   * 
   * @param countSubTestSuites
   * @return
   */
  public int numberOfTestSuites(boolean countSubTestSuites)
  {    
    LOGGER.trace("Retrieving number of child test suites. Direct childs: {}", () -> testSuites.size());
    
    int iCount = testSuites.size();
    
    if(countSubTestSuites)  
    {
      for(Map.Entry<String, TestSuite> entry : testSuites.entrySet())
      {
        iCount += entry.getValue().numberOfTestSuites(countSubTestSuites);
      }
    }
    
    return iCount;
  }

  /**
   * Check if a sub test suite is part of this test suite 
   * 
   * @param testSuite
   * @return
   */
  public boolean hasTestSuite(TestSuite testSuite)
  {
    return hasTestSuite(testSuite, false);
  }
  
  /**
   * Check if a test case is part of this test suite. When
   * searchSubTestSuites is set to true all sub-suites are searched too.
   * 
   * @param testSuite
   * @param searchSubTestSuites
   * @return
   */
  public boolean hasTestSuite(TestSuite testSuite, boolean searchSubTestSuites)
  {
    LOGGER.trace("Test if this test suite has a child {} ({})", () -> testSuite.getId(), () -> testSuite.toString());
    
    if(testSuite == this)
    {
      return true;
    }
    
    for(Map.Entry<String, TestSuite> entry : testSuites.entrySet())
    {
      if(entry.getValue().equals(testSuite))
      {
        return true;
      }
      
      if(searchSubTestSuites &&
         entry.getValue().hasTestSuite(testSuite, searchSubTestSuites))
      {
        return true;
      }
    }

    return false;
  }

  /**
   * Get a map with all test suites
   * 
   * @return
   */
  public LinkedHashMap<String,TestSuite> getTestSuites()
  {
    return testSuites;
  }

  /**
   * Get a child test suite based on it's identifier
   * 
   * @param testSuiteId
   * @return
   * @throws Exception
   */
  public TestSuite getTestSuite(String id) throws Exception
  {
    return testSuites.get(id);
  }

    
  /**
   * Add a new test case to this test suite
   * 
   * @param testCase
   * @throws Exception
   */
  public void addTestCase(TestCase testCase) throws Exception
  {    
    LOGGER.trace("Add child test suite '{}'", () -> testCase.getId());
  
    if(!this.hasTestCase(testCase))
    {
      this.testCases.put(testCase.getId(), testCase);
    }
    else
    {
      throw new Exception("TestSuite " + testCase.getId() + " is already a member of this testsuite.");
    }
  }

  /**
   * Get a specific test case based on it's id
   * 
   * @param key
   * @return
   * @throws Exception
   */
  public TestCase getTestCase(String key) throws Exception
  {
    return testCases.get(key);
  }
  
  /**
   * Get the number of test cases of this test suite
   * 
   * @return
   */
  public int numberOfTestCases()
  {
    return numberOfTestCases(false);
  }

  /**
   * Get the number of test cases of this test suite. When 
   * countSubTestCases is set to true all sub-cases are counted too.
   * 
   * @param countSubTestCases
   * @return
   */
  public int numberOfTestCases(boolean countSubTestCases)
  {
    LOGGER.trace("Retrieving number of child test cases. Direct childs: {}", () -> testCases.size());
    
    int iCount = testCases.size();
    
    if(countSubTestCases)  
    {
      for(Map.Entry<String, TestSuite> entry : testSuites.entrySet())
      {
        iCount += entry.getValue().numberOfTestCases(countSubTestCases);
      }
    }
    
    return iCount;
  }
  
  /**
   * Check if a test case is part of this test suite 
   * 
   * @param testCase
   * @return
   */
  public boolean hasTestCase(TestCase testCase)
  {
    return this.testCases.containsValue(testCase);
  }

  /**
   * Check if a test case is part of this test suite 
   * 
   * @param id
   * @return
   */
  public boolean hasTestCase(String id)
  {
    return this.testCases.containsKey(id);
  }
  
  /**
   * Get a map with all test cases of this test suite
   * 
   * @return
   */
  public LinkedHashMap<String,TestCase> getTestCases()
  {
    return testCases;
  }
 
  /**
   * Execute all tests in this test suite and sub test suites
   */
  public void execute()
  {
    try
    {
      LOGGER.debug("RUN:     " + this.toString());
      
      // First execute all test cases
      for(Map.Entry<String, TestCase> entry : testCases.entrySet())
      {
        try
        {
          entry.getValue().execute();
        }
        catch(SetUpActionException eX)
        {
          LOGGER.warn(eX);
        }
        catch(TestActionException eX)
        {
          LOGGER.debug(eX);
        }
        catch(TearDownActionException eX)
        {
          LOGGER.warn(eX);
        }
        catch(Exception eX)
        {
          LOGGER.error(eX);
        }
      }
      
      // Second, execute all sub test suites
      for(Map.Entry<String, TestSuite> entry : testSuites.entrySet())
      {
        try
        {
          entry.getValue().execute();
        }
        catch(Exception eX)
        {
          LOGGER.error(eX);
        }
      }
    }
    catch(Exception eX)
    {
      LOGGER.error(eX);
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
    return String.format( "TS: ID: %s%s (TC: %d, TS: %d)", 
                          id, 
                          parent != null ? ", parent: " + parent.getId() : "",
                          numberOfTestCases(), 
                          numberOfTestSuites());
  }
}
