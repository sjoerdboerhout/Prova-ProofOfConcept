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
 * Date:      18-07-2018
 * Author(s): Robert Bralts
 * <p>
 */
package nl.dictu.prova.plugins.reporting.csvreport;


import nl.dictu.prova.Config;
import nl.dictu.prova.TestRunner;
import nl.dictu.prova.framework.TestAction;
import nl.dictu.prova.framework.TestCase;
import nl.dictu.prova.framework.TestStatus;
import nl.dictu.prova.framework.TestSuite;
import nl.dictu.prova.plugins.reporting.ReportingPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.channels.FileChannel;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;


public class CsvReport implements ReportingPlugin
{
  final static Logger LOGGER = LogManager.getLogger();
  private PrintWriter pwTestcase;
  private PrintWriter pwTestsuite;
  private PrintWriter pwSummary;
  private TestRunner testRunner;
  
  private String testRoot;
  private String reportRoot;
  private String currTestSuiteDir;
  private String currTestCaseFile;
  //private String fileName;
  private Long startTime;
  private Long startTimeTestsuite;
  private Boolean summaryCreated = false;
  private Long startTimeSummary;
  private Integer countPassedTestcases = 0;
  private Integer countFailedTestcases = 0;
	private String testProject;

  @Override
  public void init(TestRunner testRunner) throws Exception
  {
    LOGGER.debug("Init: reporting plugin CSV Report!");
    
    if(testRunner == null)
       throw new Exception("No testRunner supplied!");
    
    this.testRunner = testRunner;
  }

  @Override
  public void setUp(String projectName) throws Exception
  {
	  try
	  {
		  LOGGER.debug("CSVReport: setUp for project '" + projectName + "'");
		  
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
		  if (Boolean.valueOf(testRunner.getPropertyValue(Config.PROVA_PLUGINS_REPORTING_CREATE_FOLDERS)))
		  {
			  reportRoot = reportRoot +
					  (projectName.length() > 0 ? (projectName + File.separator) : "") +
					  LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy")).toString() +
					  File.separator +
					  LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM-MMM")).toString() +
					  File.separator +
					  LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd")).toString() +
					  File.separator +
					  LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH-mm-ss")).toString();

			  new File(reportRoot).mkdirs();
		  }
		  else
		  {
			  LOGGER.debug("Not creating folders as requested. Reports will be placed in: "+reportRoot);
              LOGGER.trace("Deleting files in reportfolder...");
			  deleteFolder(dir);
              LOGGER.trace("files in reportfolder deleted...");
		  }
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
  public void logStartTest(TestCase testCase) throws Exception
  {
	  try {
	      String testCaseFilename = "";
	      String testCaseSheetname = "";
	      String testCaseDataSetFileName = "";
	      String testCaseDataSetSheetName = "";
	      String testCaseDataSetColumnName = "";
	      String tmp;
	      int iIndex;
	      boolean hasDataFile = false;
      
		  LOGGER.debug("CSVReport: Start new Test Case: '{}'", testCase.getId());
		  
		  //LOGGER.fatal("TestRoot: {}", testRoot);
      //LOGGER.fatal("currTestSuiteDir: {}", currTestSuiteDir);
      
      //currTestCaseFile = currTestSuiteDir;
      iIndex = testCase.getId().lastIndexOf(File.separator + File.separator);
      if( iIndex > 0)  
      {
        tmp = testCase.getId().substring(0, testCase.getId().lastIndexOf(File.separator + File.separator));
        hasDataFile = true;
      }
      else
      {
        tmp = testCase.getId();
      }
      
      iIndex = tmp.lastIndexOf(File.separator);
      
      testCaseSheetname = tmp.substring(iIndex + File.separator.length());
      
      tmp = tmp.substring(0, iIndex);
      // TODO: Change 5 with length of file extension test scripts!
      testCaseFilename = tmp.substring(tmp.lastIndexOf(File.separator) + File.separator.length(), tmp.length() - 5);
      
      // Find dataset if available
      if(hasDataFile)  
      {
        tmp = testCase.getId().substring(testCase.getId().lastIndexOf(File.separator + File.separator) + (File.separator.length() *2));
        testCaseDataSetFileName = tmp.substring(0, tmp.indexOf(".xlsx"));
        
        tmp = tmp.substring(tmp.indexOf(File.separator) + File.separator.length());
        
        testCaseDataSetSheetName = tmp.substring(0, tmp.indexOf(File.separator));
        testCaseDataSetColumnName = tmp.substring(tmp.indexOf(File.separator) + File.separator.length());
      }
      
      LOGGER.trace("testCaseFilename: {}", testCaseFilename);
      LOGGER.trace("testCaseSheetname: {}", testCaseSheetname);
      LOGGER.trace("testCaseDataSetFileName: {}", testCaseDataSetFileName);
      LOGGER.trace("testCaseDataSetSheetName: {}", testCaseDataSetSheetName);
      LOGGER.trace("testCaseDataSetColumnName: {}", testCaseDataSetColumnName);
      
      currTestCaseFile = currTestSuiteDir +
                         File.separator +
                         testCaseFilename +                        
                         File.separator +
                         testCaseSheetname +
                         (testCaseDataSetFileName.length() > 0 ? File.separator : "") +
                         testCaseDataSetFileName + 
                         (testCaseDataSetSheetName.length() > 0 ? File.separator : "") +
                         testCaseDataSetSheetName +
                         (testCaseDataSetColumnName.length() > 0 ? File.separator : "") +
                         testCaseDataSetColumnName +
                         ".csv";
          
      LOGGER.trace("Log file name for test case: '{}'", currTestCaseFile);
      
      tmp = currTestCaseFile.substring(0, currTestCaseFile.lastIndexOf(File.separator));
      LOGGER.trace("Create directory for test case: '{}'", tmp);
      new File(tmp).mkdirs();
		  
		  LOGGER.debug("Write begin testcase (r)");
		  File file = new File(currTestCaseFile);
    	  if(!file.exists())
    	  {
    		  LOGGER.trace("Creating file: '" + file + "'");
    		  file.createNewFile();
    	  }
    	  FileWriter fw = new FileWriter(file,true);
    	  BufferedWriter bw = new BufferedWriter(fw);
    	  //pwTestcase = new PrintWriter(bw);*/
    	  pwTestcase = createPW(currTestCaseFile);
    	  pwTestcase.println("Action;Step;Status;Time");

	  }
	  catch(IOException eX)
	  {
		  LOGGER.debug("Write file has failed; " + eX);
		  throw eX;
	  }
  }


	
	@Override
	public void logAction(TestAction action, String status, long executionTime) throws Exception {

		String sAction = action.toString();
		if (sAction.toLowerCase().contains("setdbproperties")||sAction.toLowerCase().contains("setsoapproperties"))
		{
            String aAction[] = sAction.split(":");
		    sAction = aAction[0] + ": ***************************************************************************************";
		}
		try {
			pwTestcase.println( sAction + ";" + (action.getId()) + ";"
					+ status + ";" + executionTime);
		} catch (Exception eX) {
			LOGGER.error("Exception in logging testAction! ({})", eX.getMessage());
			pwTestcase
					.println("error;error;error");
		}
		pwTestcase.flush();
	}

  @Override
  public void logEndTest(TestCase testCase) throws Exception
  {
	  // Write end testcase report and close stream

	  pwTestcase.close();
    
  }

  @Override
  public void logMessage(String message) throws Exception
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void logMessage(String[] messages) throws Exception
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void shutDown()
  {
    //Close summary report file
	try
    {
    	LOGGER.debug("shutdown");
    	//pwSummary.close();
    }
    catch(Exception eX)
    {
    	LOGGER.debug("Writer already has been closed");
    }
    
  }

  @Override
  public void logStartTestSuite(TestSuite testSuite) throws Exception
  {
	// Create report testsuite
	LOGGER.debug("logStartTestSuite - TestRoot: " + testRoot);
	LOGGER.debug("TestsuiteID = " + testSuite.getId());
	String testSuiteSubDir = testSuite.getId().substring(testSuite.getId().lastIndexOf(File.separator)+1);
	
	//LOGGER.debug("Testsuite sub dir = " + testSuiteSubDir);  
  //LOGGER.debug("testroot = " + testRoot);
  try
  {
    if(testSuiteSubDir.substring(testSuiteSubDir.length() - 1) == File.separator)
      testSuiteSubDir = testSuiteSubDir.replaceAll(testRoot + File.separator, "");
    else
      testSuiteSubDir = testSuiteSubDir.replaceAll(testRoot, "");
  }
  catch(Exception ex) {}
	
	LOGGER.debug("logStartTestSuite - Test Suite sub dir: " + testSuiteSubDir);
	
	if(testSuiteSubDir.length() > 0)
	{
		currTestSuiteDir = reportRoot + File.separator + testSuiteSubDir;
	  /*LOGGER.debug("Write begin testsuite ({})", currTestSuiteDir);
	  new File(currTestSuiteDir).mkdirs();
	  
	  if (testSuite.numberOfTestCases(false) > 0)
		{
		  new File(reportRoot + File.separator + testSuiteSubDir).mkdirs();
			
		  pwTestsuite = createPW(	reportRoot +
				  				 	File.separator +
				  				 	testSuiteSubDir +
				  				 	File.separator +
				  				 	testSuiteSubDir +
				  					"_overall_result.csv");

		  pwTestsuite.println("Result;Testcase;Details");
		}	*/
	}
  }

  @Override
  public void logEndTestSuite(TestSuite testSuite) throws Exception
  {
	// Create report testsuite
	  //if (testSuite.numberOfTestCases(false) > 0)
	//	{
	//	  pwTestsuite.close();
	//	}
    
  }
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
  private static void copyFileUsingChannel(File source, File dest) throws IOException {
	    FileChannel sourceChannel = null;
	    FileChannel destChannel = null;
	    try {
	        sourceChannel = new FileInputStream(source).getChannel();
	        destChannel = new FileOutputStream(dest).getChannel();
	        destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
	       }finally{
	           sourceChannel.close();
	           destChannel.close();
	       }
	}
    @Override
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
            for(File f: files)
            {
                if (f.isDirectory())
                {
                    deleteFolder(f);
                }
                else{
                    f.delete();
                }

            }
        }
    }
	

}
