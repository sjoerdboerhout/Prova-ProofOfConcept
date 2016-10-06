package nl.dictu.prova.plugins.reporting.simplereport;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import nl.dictu.prova.Config;
import nl.dictu.prova.TestRunner;
import nl.dictu.prova.framework.TestAction;
import nl.dictu.prova.framework.TestCase;
import nl.dictu.prova.framework.TestSuite;
import nl.dictu.prova.plugins.reporting.ReportingPlugin;

/*
 * Hello world!
 *
 */
public class SimpleReport implements ReportingPlugin
{
  final static Logger LOGGER = LogManager.getLogger();
  private PrintWriter pwTestcase;
  private PrintWriter pwTestsuite;
  private PrintWriter pwSummary;
  private TestRunner testRunner;
  private String outputDirectory = "";
  private String parentDirectory = "";
  private String fileName;
  private Long startTime;
  private Long startTimeTestsuite;
  private Boolean summaryCreated = false;
  private Long startTimeSummary;
  private Integer countPassedTestcases = 0;
  private Integer countFailedTestcases = 0;

  @Override
  public void init(TestRunner testRunner) throws Exception
  {
    LOGGER.debug("Init: reporting plugin Simple Report!");
    
    if(testRunner == null)
       throw new Exception("No testRunner supplied!");
    
    this.testRunner = testRunner;
  }

  @Override
  public void setOutputDir(String outputDir) throws Exception
  {
    try
    {
    	LOGGER.debug("Setting up Outputdirectory. Current value: "+ outputDirectory);
    	// setting parent folder if outputdirectory is set for the first time in the testrun
    	if (outputDirectory.equals(""))
    	{
    		DateTimeFormatter sdf = DateTimeFormatter.ofPattern("yyyyMMdd_hhmmss");
    		outputDirectory = testRunner.getPropertyValue(Config.PROVA_PLUGINS_REPORTING_DIR)+ "\\" + "Testrun_" + outputDir + "_" + LocalDateTime.now().format(sdf).toString();
    		parentDirectory = outputDirectory;
    		LOGGER.debug("Parentdirectory set to: " + parentDirectory);
    		LOGGER.debug("(IF) Outputdirectory set to: " + outputDirectory);
    	}
    	// append after parentfolder
    	else
    	{
    		outputDirectory = parentDirectory + "\\" + outputDir;
    		LOGGER.debug("(ELSE) Outputdirectory set to: " + outputDirectory);
    	}
    }
    catch(Exception Ex)
    {
    	LOGGER.debug("Failed to determine output directory" + Ex);
    	throw Ex;
    }
    
  }

  @Override
  public void setUp(String fileName) throws Exception
  {
	  LOGGER.debug("Set up output directory");
	  try
	  {
		  setOutputDir(fileName);
	  }
	  catch(Exception eX)
	  {
		  throw eX;
	  }
	  try
	  {
		  LOGGER.trace("Check if report folder exists");
		  File dir = new File(outputDirectory);
		  // Check if dir exists, if note create it
		  if (!dir.isDirectory())
		  {
			  LOGGER.trace("Creating report folder: '" + outputDirectory + "'");
			  dir.mkdirs();
		  }
		  // Create summary report if not yet created
		  if (!summaryCreated)
		  {
	    	  pwSummary = createPW(outputDirectory+"/Testrun_Summary.html");
	    	  pwSummary.println("<!DOCTYPE html>");
	    	  pwSummary.println("<html>");
	    	  pwSummary.println("<head>");
	    	  pwSummary.println("<style> table, td { 	border: 1px solid black;	border-collapse: collapse;	font-family: Verdana, Helvetica, sans-serif;	font-size: 15px;}"
			  							+ "th {	text-align: left;	font-family: Verdana, Helvetica, sans-serif;	font-size: 15px;}"
			  							+ "tr:nth-child(odd) {	background: #CBCDCD;}"
			  							+ "p {	font-family: Verdana, Helvetica, sans-serif;	font-size: 15px;}"
			  							+ "br {	font-family: Verdana, Helvetica, sans-serif;	font-size: 15px;}"
			  							+ "h1 {	font-family: Verdana, Helvetica, sans-serif;	font-size: 30px;}</style>");
	    	  pwSummary.println("<title>Prova Testreport</title>");
	    	  pwSummary.println("</head>");
	    	  pwSummary.println("<body>");	 
	    	  pwSummary.println("<h1>Testrun Summary</h1>");
			  startTimeSummary = System.currentTimeMillis();
			  pwSummary.println("<br><b>Starttime: </b>" + LocalDateTime.now() +"</br>");
			  pwSummary.println("<table>			<tr>				<th>Testcase</th><th>Error</th><th>Details</th></tr>");
			  summaryCreated = true;
		  }
		   
	  }
	  
		  
	  catch(Exception eX)
	  {
		  LOGGER.debug("Setup has failed; " + eX);
	  }
	  
	  
  }

  @Override
  public void logStartTest(TestCase testCase) throws Exception
  {
	  try
	  {
		  //LOGGER.debug("Start SimpleReport Setup");
		  //this.setUp(testCase.getId().substring(testCase.getId().lastIndexOf("\\")+1));
		  LOGGER.debug("Write begin testcase (r)");
		  fileName = outputDirectory+"/"+ testCase.getId().substring(testCase.getId().lastIndexOf("\\")+1) + ".html";
		  File file =new File(fileName);
    	  if(!file.exists())
    	  {
    		  LOGGER.trace("Creating file: '" + file + "'");
    		  file.createNewFile();
    	  }
    	  FileWriter fw = new FileWriter(file,true);
    	  BufferedWriter bw = new BufferedWriter(fw);
    	  //pwTestcase = new PrintWriter(bw);*/
    	  pwTestcase = createPW(fileName);
    	  pwTestcase.println("<!DOCTYPE html>");
    	  pwTestcase.println("<html>");
    	  pwTestcase.println("<head>");
    	  pwTestcase.println("<style> table, td { 	border: 1px solid black;	border-collapse: collapse;	font-family: Verdana, Helvetica, sans-serif;	font-size: 15px;}"
		  							+ "th {	text-align: left;	font-family: Verdana, Helvetica, sans-serif;	font-size: 15px;}"
		  							+ "tr:nth-child(odd) {	background: #CBCDCD;}"
		  							+ "p {	font-family: Verdana, Helvetica, sans-serif;	font-size: 15px;}"
		  							+ "br {	font-family: Verdana, Helvetica, sans-serif;	font-size: 15px;}"
		  							+ "h1 {	font-family: Verdana, Helvetica, sans-serif;	font-size: 30px;}</style>");
    	  pwTestcase.println("<title>Prova Testreport</title>");
    	  pwTestcase.println("</head>");
    	  pwTestcase.println("<body>");	 
		  pwTestcase.println("<h1>"+testCase.getId().substring(testCase.getId().lastIndexOf("\\")+1)+"</h1>");
		  startTime = System.currentTimeMillis();
		  pwTestcase.println("<br><b>Starttime: </b>" + LocalDateTime.now() +"</br>");
		  pwTestcase.println("<table>			<tr>				<th>Result</th><th>Action</th><th>Info</th></tr>");
		  
		  //this.shutDown();
	  }
	  catch(IOException eX)
	  {
		  LOGGER.debug("Write file has failed; " + eX);
	  }
  }

  @Override
  public void logAction(TestAction action, String status) throws Exception
  {
	  String color = "red";
	  if (status.equalsIgnoreCase("ok"))
	  {
		  color = "lightgreen";
	  }

	  try
          {
                pwTestcase.println("<tr><td style=\"width:200px\" bgcolor=\""+color+"\">"+status+"</td><td style=\"width:1200px\">"+action.toString()+"</td><td style=\"width:200px\">" + (action.getId()) +"</td></tr>");
          }
          catch(Exception eX)
          {
                LOGGER.error("Exception in logging testAction!");
                pwTestcase.println("<tr><td style=\"width:200px\" bgcolor=\""+color+"\">N/A</td><td style=\"width:1200px\">UNKNOWN ACTION</td><td style=\"width:200px\">Unknown action id</td></tr>");
          }
  }

  @Override
  public void logEndTest(TestCase testCase) throws Exception
  {
	  // Write end testcase report and close stream
	  Long elapsedTime = System.currentTimeMillis() - startTime;
	  pwTestcase.println("<br><b>Endtime: </b>" + LocalDateTime.now()+"</br>");
	  pwTestcase.println("<br><b>Runtime in seconds: </b>" + elapsedTime/1000 + "</br>");
	  if (testCase.getStatus().toString().equalsIgnoreCase("passed"))
	  {
		  countPassedTestcases = countPassedTestcases + 1;
		  pwTestcase.println("<br><b>Status testcase: <font color=\"green\">" + testCase.getStatus()+"</b></font></br>" );
		  pwTestsuite.println("<tr><td style=\"width:200px\" bgcolor=\"lightgreen\">"+testCase.getStatus()+"</td><td style=\"width:1200px\">"
				  				+testCase.getId().substring(testCase.getId().lastIndexOf("\\")+1)+"</td><td style=\"width:200px\">" 
				  				+ "<a href=\""+ fileName
				  				+ "\">Resultaat testgeval</a></td></tr>");
	  }
	  else
	  {
		  countFailedTestcases = countFailedTestcases + 1;
		  pwTestcase.println("<br><b>Status testcase: <font color=\"red\">" + testCase.getStatus()+"</b></font></br>" );
		  pwTestsuite.println("<tr><td style=\"width:200px\" bgcolor=\"red\">"+testCase.getStatus()+"</td><td style=\"width:1200px\">"
	  				+testCase.getId().substring(testCase.getId().lastIndexOf("\\")+1)+"</td><td style=\"width:200px\">" 
	  				+ "<a href=\""+fileName
	  				+ "\">Resultaat testgeval</a></td></tr>");
		  pwSummary.println("<tr><td style=\"width:200px\" bgcolor=\"red\">"+testCase.getId().substring(testCase.getId().lastIndexOf("\\")+1)
				    +"</td><td style=\"width:1200px\">"
	  				+testCase.getSummary()+"</td><td style=\"width:200px\">" 
	  				+ "<a href=\""+fileName
	  				+ "\">Resultaat testgeval</a></td></tr>");
	  }
	  pwTestcase.println("<br><b>Summary: </b>" + testCase.getSummary()+"</br>");
	  pwTestcase.println("</table>");
	  pwTestcase.println("</body>");
	  pwTestcase.println("</html>");
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
    	Long elapsedTime = System.currentTimeMillis() - startTimeSummary;
    	pwSummary.println("<br><b>Endtime: </b>" + LocalDateTime.now()+"</br>");
    	pwSummary.println("<br><b>Runtime in seconds: </b>" + elapsedTime/1000 + "</br>");
    	pwSummary.println("<br><b>Testcases Processed: </b>" + (countPassedTestcases + countFailedTestcases) + "</br>");
    	pwSummary.println("<br><b>Testcases Passed: </b>" + countPassedTestcases + "</br>");
    	pwSummary.println("<br><b>Testcases Failed: </b>" + countFailedTestcases + "</br>");
    	pwSummary.println("</table>");
    	pwSummary.println("</body>");
    	pwSummary.println("</html>");
    	pwSummary.close();
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
	setUp(testSuite.getId().substring(testSuite.getId().lastIndexOf("\\")+1));
    LOGGER.debug("Write begin testsuite");
	if (testSuite.numberOfTestCases(false) > 0)
	{
		  pwTestsuite = createPW(outputDirectory+"/"+ testSuite.getId().substring(testSuite.getId().lastIndexOf("\\")+1) + "_overall_result.html");
		  pwTestsuite.println("<!DOCTYPE html>");
		  pwTestsuite.println("<html>");
		  pwTestsuite.println("<head>");
		  pwTestsuite.println("<style> table, td { 	border: 1px solid black;	border-collapse: collapse;	font-family: Verdana, Helvetica, sans-serif;	font-size: 15px;}"
		  							+ "th {	text-align: left;	font-family: Verdana, Helvetica, sans-serif;	font-size: 15px;}"
		  							+ "tr:nth-child(odd) {	background: #CBCDCD;}"
		  							+ "p {	font-family: Verdana, Helvetica, sans-serif;	font-size: 15px;}"
		  							+ "br {	font-family: Verdana, Helvetica, sans-serif;	font-size: 15px;}"
		  							+ "h1 {	font-family: Verdana, Helvetica, sans-serif;	font-size: 30px;}</style>");
		  pwTestsuite.println("<title>Prova Testreport</title>");
		  pwTestsuite.println("</head>");
		  pwTestsuite.println("<body>");	 
		  pwTestsuite.println("<h1>"+testSuite.getId().substring(testSuite.getId().lastIndexOf("\\")+1)+"</h1>");
		  startTimeTestsuite = System.currentTimeMillis();
		  pwTestsuite.println("<br><b>Starttime: </b>" + LocalDateTime.now() +"</br>");
		  pwTestsuite.println("<table>			<tr>				<th>Result</th><th>Testcase</th><th>Details</th></tr>");
	}	  
  }

  @Override
  public void logEndTestSuite(TestSuite testSuite) throws Exception
  {
	// Create report testsuite
	  if (testSuite.numberOfTestCases(false) > 0)
		{
		  Long elapsedTime = System.currentTimeMillis() - startTimeTestsuite;
		  pwTestsuite.println("<br><b>Endtime: </b>" + LocalDateTime.now()+"</br>");
		  pwTestsuite.println("<br><b>Runtime in seconds: </b>" + elapsedTime/1000 + "</br>");
		  pwTestsuite.println("</table>");
		  pwTestsuite.println("</body>");
		  pwTestsuite.println("</html>");
		  pwTestsuite.close();
		}
    
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

  @Override
  public void storeToTxt(String text, String name) throws Exception
  {
    try
    {
      File folder = new File(outputDirectory + File.separator + "txt" + File.separator);
      folder.mkdir();
      
      String file = outputDirectory + File.separator + "txt" + File.separator + name + ".txt";
      PrintWriter printWriter = createPW(file);
      printWriter.println(text);
      printWriter.flush();
      printWriter.close();
      //pwTestcase.println("<tr><td style=\"width:200px\" bgcolor=\"lightgreen\">N/A</td><td style=\"width:1200px\">UNKNOWN ACTION</td><td style=\"width:200px\">Stored message/query with name " + name + " to text file at <a href=\"" + file + "\">this location</a></td></tr>");
      LOGGER.info("Stored message/query with name " + name + " to text file at '" + file + "'");
    }
    catch(Exception ex){
      LOGGER.error("Exception while writing message/query to txt file! : " + ex.getMessage());
    }
  }
}
