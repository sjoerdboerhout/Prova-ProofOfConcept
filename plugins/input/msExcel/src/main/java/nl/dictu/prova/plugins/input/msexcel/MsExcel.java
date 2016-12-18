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
package nl.dictu.prova.plugins.input.msexcel;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import nl.dictu.prova.Config;
import nl.dictu.prova.TestRunner;
import nl.dictu.prova.framework.TestCase;
import nl.dictu.prova.framework.TestSuite;
import nl.dictu.prova.plugins.input.InputPlugin;
import nl.dictu.prova.plugins.input.msexcel.builder.TestCaseBuilder;
import nl.dictu.prova.plugins.input.msexcel.builder.TestSuiteBuilder;

/**
 * Input plugin for Prova for reading test scripts from MS Excel files
 * 
 * @author Sjoerd Boerhout
 * @since 2016-04-25
 */
public class MsExcel implements InputPlugin
{
  final static Logger LOGGER = LogManager.getLogger();
  
  private TestRunner testRunner = null;
  
  private File       testRoot   = null;
  private String[]   labels     = null;
  
  /**
   * Init the plug-in and check if a valid reference to a testRunner
   * was given
   */
  @Override
  public void init(TestRunner testRunner) throws Exception
  {
    LOGGER.debug("Init: input plugin MsExcel reader!");
    
    if(testRunner == null)
       throw new Exception("No testRunner supplied!");
    
    this.testRunner = testRunner;
  }

  
  /**
   * Set the root location of the test scripts and validate
   * it can be read correctly.
   */
  @Override
  public void setTestRoot(String newTestRoot, String projectName) throws Exception
  {
    try
    { 
      LOGGER.trace("Set new test root: '{}'", newTestRoot);
    	
      // First remove space before and after the test root
      newTestRoot = newTestRoot.trim();
      projectName = projectName.trim();
      
      File testRoot = new File(newTestRoot);
      
      if(!testRoot.isDirectory())
      {
        LOGGER.trace("Test root not found. Try as sub-directory of Prova's default test root.");
        testRoot = new File(testRunner.getPropertyValue(Config.PROVA_DIR) + System.getProperty("file.separator") + newTestRoot);
      }
      
      if(!testRoot.isDirectory())
        throw new Exception("Test root must be a directory! (" + newTestRoot + ")");
      
      if(!testRoot.canRead())
        throw new Exception("Test root can not be read! (" + newTestRoot + ")");

      // Save new test root
      testRunner.setPropertyValue(Config.PROVA_TESTS_ROOT, testRoot.getAbsolutePath());

      // Check if a directory for the given project name exists
      testRoot = new File(testRunner.getPropertyValue(Config.PROVA_TESTS_ROOT) + System.getProperty("file.separator") + projectName);
      LOGGER.trace("Test if a project dir is available: '{}'", testRoot.getAbsolutePath());
      if(testRoot.isDirectory())
      {
        LOGGER.trace("Project dir is available: '{}'", testRoot.getAbsolutePath());
        testRunner.setPropertyValue(Config.PROVA_TESTS_ROOT, testRoot.getAbsolutePath());
      }
      
      LOGGER.info("Active test root: '{}'", testRunner.getPropertyValue(Config.PROVA_TESTS_ROOT));
    }
    catch(Exception eX)
    {
      throw new Exception("Unable to read test root '" + newTestRoot + "'");
    }
  }

  @Override
  public void setLabels(String[] labels)
  {
    LOGGER.debug("Set filters for labels to '{}'", () -> labels);
    this.labels = labels;
  }

  @Override
  public void setTestCaseFilter(String[] testCases)
  {
    // TODO Auto-generated method stub

  }

  @Override
  public void setUp() throws Exception
  {
    LOGGER.debug("SetUp: input plugin MsExcel reader! ({})", testRunner.getPropertyValue(Config.PROVA_TESTS_ROOT));
    
    try
    {
      TestSuiteBuilder testSuiteBuilder = new TestSuiteBuilder();
      TestSuite testSuite = null;
      
      testSuite = testSuiteBuilder.buildTestSuite(new File(testRunner.getPropertyValue(Config.PROVA_TESTS_ROOT)),
                                   this.testRunner);
      
      testRunner.setRootTestSuite(testSuite);
    }
    catch(Exception eX)
    {
      throw eX;
    }
  }

  
  @Override
  public void loadTestCase(TestCase testCase) throws Exception
  {
    try
    {
      LOGGER.debug("Load TC: '{}'", () -> testCase.toString());
      
      TestCaseBuilder testCaseBuilder = new TestCaseBuilder(testRunner.getPropertyValue(Config.PROVA_TESTS_ROOT), this.testRunner);
      
      testCaseBuilder.buildTestCase(testCase);
      LOGGER.info("Loaded TC: '{}'", () -> testCase.toString());
    }
    catch(Exception eX)
    {
      LOGGER.error("Exception while loading test case '{}'", testCase.toString(), eX);
      throw eX;
    }
  }

  @Override
  public void shutDown()
  {
    // TODO Auto-generated method stub
    LOGGER.debug("ShutDown input plugin MsExcel");
  }

  @Override
  public String getName()
  {
    return "MsExcel";
  }
}
