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
package nl.dictu.prova.plugins.output.web.scriptprinter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;


import nl.dictu.prova.Config;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import nl.dictu.prova.TestRunner;
import nl.dictu.prova.framework.TestCase;
import nl.dictu.prova.framework.parameters.Xpath;
import nl.dictu.prova.plugins.output.OutputPlugin;
import nl.dictu.prova.plugins.output.ShellOutputPlugin;
import nl.dictu.prova.plugins.output.WebOutputPlugin;
import nl.dictu.prova.plugins.output.SoapOutputPlugin;

/**
 * Output plugin to print all actions to a file
 *
 * @author Sjoerd Boerhout
 *
 */
public class ScriptPrinter implements WebOutputPlugin
{

  private String testRoot;
  private String reportRoot;
  private String testProject;
  private String TCID;
  private Boolean summaryCreated = false;
  final static Logger LOGGER = LogManager.getLogger();

  private TestRunner testRunner = null;

  /**
   * Init the plug-in and check if a valid reference to a testRunner was given
   */
  @Override
  public void init(TestRunner testRunner) throws Exception
  {
    LOGGER.debug("Init: output plugin ScriptPrinter!");

    if (testRunner == null)
    {
      throw new Exception("No testRunner supplied!");
    }

    this.testRunner = testRunner;
  }

  @Override
  public void setUp(TestCase testCase) throws Exception
  {
    LOGGER.debug("Setup: Test Case ID '{}'", () -> testCase.getId());
    // TODO start new file
    System.out.println("==================================================");
    System.out.println("Start of TC: '" + testCase.getId() + "'\n");

    try
    {
      // Save the test root to strip that part from the test suite/case names.
      this.testRoot = testRunner.getPropertyValue(Config.PROVA_TESTS_ROOT);

      // save project name, needed for making html links relative
      this.testProject = testRunner.getPropertyValue(Config.PROVA_PROJECT);
      /*
       * - Check if property 'prova.plugins.reporting.dir' is an existing dir.
       * - if not: Try to create it as a sub-dir in the Prova root dir.
       */
      String dirName = testRunner.getPropertyValue(Config.PROVA_PLUGINS_REPORTING_DIR);
      File dir = new File(dirName);

      // Check if configured path is an absolute and existing path
      if(checkValidReportDir(dir))
      {
        LOGGER.debug("Set up output directory for reporting to '" + dir.getAbsolutePath() + "'");
      }
      else
      {
        if (Boolean.valueOf(testRunner.getPropertyValue(Config.PROVA_PLUGINS_REPORTING_CREATE_FOLDERS))) {
          dirName = testRunner.getPropertyValue(Config.PROVA_DIR) + File.separator +
                  testRunner.getPropertyValue(Config.PROVA_PLUGINS_REPORTING_DIR)+ File.separator +"csvreport";
        }
        else
        {
          dirName = testRunner.getPropertyValue(Config.PROVA_PLUGINS_REPORTING_DIR)+ File.separator +"csvreport";
        }
        dir = new File(dirName);

        // Try if the configured path is a sub-directoy of the Prova root path
        if(checkValidReportDir(dir))
        {
          LOGGER.debug("Set up output directory for reporting to '" + dir.getAbsolutePath() + "'");
        }
        else
        {
          // Dir doesn't exists. Create it!
          dir.mkdirs();
          LOGGER.debug("Created output directory '" + dir.getAbsolutePath() + "' for reporting to.");
        }
      }
      reportRoot = dir.getAbsolutePath() + File.separator;
      // Create a directory for this testrun
     LOGGER.debug("Not creating folders as requested. Reports will be placed in: "+reportRoot);
     LOGGER.trace("Deleting files in reportfolder...");
     deleteFolder(dir);
     LOGGER.trace("files in reportfolder deleted...");

     //Get name of testproject and invidual testcases
     testRoot = testCase.getId();
     String[] testCaseName = testRoot.split("\\\\");
     TCID = testCaseName[testCaseName.length-1];
     storeToTxt("Testactions in this testcase: ", TCID);

      // Create the start of the summary file
      //pwSummary = createPW(reportRoot + File.separator + "Testrun_Summary.csv");
      //pwSummary.println("<!DOCTYPE html>");

      summaryCreated = true;
    }
    catch(Exception eX)
    {
      LOGGER.debug("Setup CSVReport has failed: " + eX);
      throw eX;
    }
  }

  private boolean checkValidReportDir(File testDir)
  {
    try
    {
      LOGGER.trace("Check if '" + testDir.getAbsolutePath() + "' is a valid Reporting dir");

      if(!testDir.exists())
      {
        LOGGER.warn("Reporting dir '" + testDir.getAbsolutePath() + "' doesn't exists.");
        return false;
      }

      if(!testDir.isDirectory())
      {
        LOGGER.warn("Reporting dir '" + testDir.getAbsolutePath() + "' is not a directory.");
        return false;
      }

      if(!testDir.canWrite())
      {
        LOGGER.warn("Reporting dir '" + testDir.getAbsolutePath() + "' is not writable.");
        return false;
      }

      LOGGER.trace("'" + testDir.getAbsolutePath() + "' is a valid Reporting dir!");
      return true;
    }
    catch(Exception eX)
    {
      return false;
    }
  }

  @Override
  public void shutDown()
  {
    // TODO Auto-generated method stub
    LOGGER.debug("Shutdown: output plugin ScriptPrinter!");
  }

  public void createScript(String outputType) {
    //functionaliteit overgenomen door setup
    //mogelijke invulling volgt later

  }

  private String getFlowPathFromTCID(String tcid) {
    LOGGER.trace("Get flow path from TCID: '{}'", tcid);

    // Split the flow and data file (if exists in tcid)
    if (tcid.contains(File.separator + File.separator)) {
      tcid = tcid.substring(0, tcid.lastIndexOf(File.separator + File.separator));
    }

    // Strip the sheet name at the end of the TCID
    LOGGER.trace("Flow path from TCID: '{}'", tcid.substring(0, tcid.lastIndexOf(File.separator)));
    return tcid.substring(0, tcid.lastIndexOf(File.separator));
  }

  //Create a file to store the generated output
  private PrintWriter createPW(String name) throws Exception
  {
    File file =new File(name);
    if(!file.exists())
    {
      LOGGER.trace("Creating file: '" + file + "'");
      file.createNewFile();
    }
    FileWriter fw = new FileWriter(file,true);
    BufferedWriter bw = new BufferedWriter(fw);
    return new PrintWriter(bw);
  }

    public void storeToTxt(String text, String name) throws Exception
    {

      try
      {
        File folder = new File(reportRoot + File.separator + "txt" + File.separator);
        folder.mkdir();

        String file = reportRoot + File.separator + "txt" + File.separator + name + ".txt";
        PrintWriter printWriter = createPW(file);
        printWriter.println(text);
        printWriter.flush();
        printWriter.close();
        LOGGER.info("Stored message/query with name " + name + " to text file at '" + file + "'");
      }
      catch(Exception ex){
        LOGGER.error("Exception while writing message/query to txt file! : " + ex.getMessage());
      }
    }

  /**
   * Replace all illegal characters in filename.
   *
   * @param fileName
   * @return
   */
  public String makeFilenameValid(String fileName) {
    return fileName.replaceAll("[^a-zA-Z0-9.-]", "_").replaceAll("_+", "_");
  }
  public static void deleteFolder(File folder){
    LOGGER.trace("Deleting files in: " + folder.getAbsolutePath());
    File[] files = folder.listFiles();
    if (files!=null)
    {
      for(File f: files )
      {
        if (f.isDirectory())
        {
          deleteFolder(f);
        }
        else if(!f.exists())
        {
          f.delete();
        }

      }
    }
  }

  @Override
  public void doSelectDropdown(String xPath, String select) throws Exception
  {
    storeToTxt("I select dropdown " + xPath + " value " + select, TCID );

   // LOGGER.trace("DoSelectDropdown '{}' ({})", () -> xPath, () -> select);
    // TODO Auto-generated method stub
    //System.out.println("Selecting '" + select + "' in element: " + xPath + "'");

  }

  @Override
  public void doSendKeys(String xPath, String keys) throws Exception
  {
    storeToTxt("Send keys '" + keys + "' to browser.",TCID );
    LOGGER.trace("DoSendKeys '{}'", () -> keys);
  }


  @Override
  public void doSetText(String xPath, String text, Boolean replace) throws Exception
  {
    storeToTxt("I fill in " + xPath + " with " + text , TCID );

    //LOGGER.trace("DoSetText '{}'", () -> text);
    // TODO Auto-generated method stub
   // System.out.println((replace ? "Replace" : "Set") + " text of '" + xPath + "' to '" + text + "'");
    //storeToTxt((replace ? "Replace" : "Set") + " text of '" + xPath + "' to '" + text + "'","ScriptPrinter" );
  }

  @Override
  public void doSleep(long waitTime) throws Exception
  {
    storeToTxt("I set timeout for " + waitTime + "' Ms"  , TCID );

    //LOGGER.trace("DoSleep for '{}' Ms", () -> waitTime);

    // TODO Auto-generated method stub
    //System.out.println("Sleep for '" + waitTime + "' Ms");
  }

  @Override
  public void doValidateElement(String xPath, Boolean exists, double timeOut) throws Exception
  {
    storeToTxt("I validate element is present " + xPath + "' "
                    + (exists ? "" : "doesn't ") + "exists." , TCID );


   // LOGGER.trace("doValidateElement '{}' ({}, {})", () -> xPath, () -> exists, () -> timeOut);
    // TODO Implement function
    //System.out.println("Validate that element '" + xPath + "' "
            //+ (exists ? "" : "doesn't ") + "exists. "
          //  + "TimeOut: " + timeOut);
  }

  @Override
  public void doValidateText(String xPath, String value, Boolean exists, double timeOut) throws Exception
  {

    storeToTxt("I validate text '" + value + "' is present " + exists +  " ,at xpath " + xPath, TCID );

    //LOGGER.trace("doValidateText '{}' ({}, {}, {})", () -> value, () -> exists, () -> timeOut, () -> xPath);
    // TODO Implement function
    //System.out.println("Validate that text '" + value + "' "
           // + (exists ? "" : "doesn't ") + "exists. "
           // + (xPath.length() > 0 ? "Element: " + xPath + ". " : "")
            //+ "TimeOut: " + timeOut);
  }

  @Override
  public String getName()
  {
    return "ScriptPrinter";
  }

  @Override
  public void doSwitchFrame(String xPath, Boolean alert, Boolean accept, String username, String password) throws Exception
  {
    storeToTxt("I SwitchFrame to " + xPath  + "' frame is alert?: "
            + (alert ? "Yes" : "No")
            + "' accept or dismiss?: "
            + (accept ? "Accept" : "Dismiss") , TCID);

   // LOGGER.trace("doValidateText '{}' ({}, {}, {})", () -> alert, () -> accept, () -> xPath, () -> username, () -> password);

    // TODO Implement function
   // System.out.println("Swithing to frame '" + xPath
           // + "' frame is alert?: "
           // + (alert ? "Yes" : "No")
           // + "' accept or dismiss?: "
          //  + (accept ? "Accept" : "Dismiss"));
    // TODO Auto-generated method stub
  }

  @Override
  public void doCaptureScreen(String string) throws Exception
  {
    // xpath, filename, paramter?
    storeToTxt("I capturescreen  " + string , TCID);
  }

  @Override
  public void doClick(String string, Boolean bln, Integer int1,  Boolean bln1, Boolean bln2) throws Exception
  {
    storeToTxt("I click on "  + string + " ,NumberOfClicks " + int1 + " ,WaitUntilPageLoaded " + bln1 + " ,ContinueOnNotFound " + bln2 , TCID);
  }

  @Override
  public void doDownloadFile(String string, String string1) throws Exception
  {
    storeToTxt("I select file to download  " + string + string1 , TCID);
  }

  @Override
  public void doSelect(String string, Boolean bln) throws Exception
  {
    storeToTxt("I fill in  " + string + " to select" , TCID);
  }

  @Override
  public void doSwitchScreen(String name) throws Exception
  {
    storeToTxt("I SwitchScreen to " + name , TCID);
  }

  @Override
  public void doNavigate(String string)throws Exception
  {
    storeToTxt("I set the location to navigate to " + string , TCID);
  }

  @Override
  public void doStoreText(String xpath, String string, String inputtext, String string1, Boolean remove, double d) throws Exception
  {
    storeToTxt("I set a parameter " + string + " to storeText " + xpath + " inputtext " + inputtext, TCID);
  }

@Override
public void doWaitForElement(String xPath, String type, Boolean exists, double timeOut) throws Exception
{
  storeToTxt("I check for element present" + exists + ", " + xPath , TCID);
}
}
