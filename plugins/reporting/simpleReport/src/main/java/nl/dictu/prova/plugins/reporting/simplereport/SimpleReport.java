package nl.dictu.prova.plugins.reporting.simplereport;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;

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
  private TestRunner testRunner;
  private String outputDirectory = "";
  private String parentDirectory = "";
  private String fileName;
  private Long startTime;

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
    	if (outputDirectory.equals(""))
    	{
    		outputDirectory = testRunner.getPropertyValue(Config.PROVA_PLUGINS_REPORTING_DIR)+ "\\" + "Testrun_" + outputDir + "_" + System.currentTimeMillis();
    		parentDirectory = outputDirectory;
    	}
    	else
    	{
    		outputDirectory = parentDirectory + "\\" + outputDir;
    	}
    }
    catch(Exception Ex)
    {
    	LOGGER.trace("Failed to determine output directory");
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
		  if (!dir.isDirectory())
		  {
			  LOGGER.trace("Creating report folder: '" + outputDirectory + "'");
			  dir.mkdirs();
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
		  LOGGER.debug("Write begin testcase");
		  fileName = outputDirectory+"/"+ testCase.getId().substring(testCase.getId().lastIndexOf("\\")+1) + "_" +System.currentTimeMillis()+".html";
		  File file =new File(fileName);
    	  if(!file.exists())
    	  {
    		  LOGGER.trace("Creating file: '" + file + "'");
    		  file.createNewFile();
    	  }
    	  FileWriter fw = new FileWriter(file,true);
    	  BufferedWriter bw = new BufferedWriter(fw);
    	  pwTestcase = new PrintWriter(bw);
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
		  pwTestcase.println("<table>			<tr>				<th>Result</th><th>Action</th><th>Excelrow</th></tr>");
		  
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

	  pwTestcase.println("<tr><td style=\"width:200px\" bgcolor=\""+color+"\">"+status+"</td><td style=\"width:1200px\">"+action.toString()+"</td><td style=\"width:200px\">" + (action.getId()+1) +"</td></tr>");
    
  }

  @Override
  public void logEndTest(TestCase testCase) throws Exception
  {
	  
	  LOGGER.debug("Status testcase: "+testCase.getStatus());
	  Long elapsedTime = System.currentTimeMillis() - startTime;
	  pwTestcase.println("<br><b>Endtime: </b>" + LocalDateTime.now()+"</br>");
	  pwTestcase.println("<br><b>Runtime in seconds: </b>" + elapsedTime/1000 + "</br>");
	  if (testCase.getStatus().toString().equalsIgnoreCase("passed"))
	  {
		  pwTestcase.println("<br><b>Status testcase: <font color=\"green\">" + testCase.getStatus()+"</b></font></br>" );
		  pwTestsuite.println("<tr><td style=\"width:200px\" bgcolor=\"lightgreen\">"+testCase.getStatus()+"</td><td style=\"width:1200px\">"
				  				+testCase.getId().substring(testCase.getId().lastIndexOf("\\")+1)+"</td><td style=\"width:200px\">" 
				  				+ "<a href=\""+ fileName
				  				+ "\">Resultaat testgeval</a></td></tr>");
	  }
	  else
	  {
		  pwTestcase.println("<br><b>Status testcase: <font color=\"red\">" + testCase.getStatus()+"</b></font></br>" );
		  pwTestsuite.println("<tr><td style=\"width:200px\" bgcolor=\"red\">"+testCase.getStatus()+"</td><td style=\"width:1200px\">"
	  				+testCase.getId().substring(testCase.getId().lastIndexOf("\\")+1)+"</td><td style=\"width:200px\">" 
	  				+ "<a href=\""+fileName
	  				+ "\">Resultaat testgeval</a></td></tr>");
	  }
	  pwTestcase.println("<br><b>Error: </b>" + testCase.getSummary()+"</br>");
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
    try
    {
    	LOGGER.debug("shutdown");
    }
    catch(Exception eX)
    {
    	LOGGER.debug("Writer already has been closed");
    }
    
  }

  @Override
  public void logStartTestSuite(TestSuite testSuite) throws Exception
  {
	this.setUp(testSuite.getId().substring(testSuite.getId().lastIndexOf("\\")+1));
    LOGGER.debug("Write begin testsuite");
	  File file =new File(outputDirectory+"/"+ testSuite.getId().substring(testSuite.getId().lastIndexOf("\\")+1) + "_overall_result.html");
	  if(!file.exists())
	  {
		  LOGGER.trace("Creating file: '" + file + "'");
		  file.createNewFile();
	  }
	  FileWriter fw = new FileWriter(file,true);
	  BufferedWriter bw = new BufferedWriter(fw);
	  pwTestsuite = new PrintWriter(bw);
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
	  startTime = System.currentTimeMillis();
	  pwTestsuite.println("<br><b>Starttime: </b>" + LocalDateTime.now() +"</br>");
	  pwTestsuite.println("<table>			<tr>				<th>Result</th><th>Testcase</th><th>Details</th></tr>");
	  
  }

  @Override
  public void logEndTestSuite(TestSuite testSuite) throws Exception
  {
	  Long elapsedTime = System.currentTimeMillis() - startTime;
	  pwTestsuite.println("<br><b>Endtime: </b>" + LocalDateTime.now()+"</br>");
	  pwTestsuite.println("<br><b>Runtime in seconds: </b>" + elapsedTime/1000 + "</br>");
	  pwTestsuite.println("</table>");
	  pwTestsuite.println("</body>");
	  pwTestsuite.println("</html>");
	  pwTestsuite.close();
    
  }
}
