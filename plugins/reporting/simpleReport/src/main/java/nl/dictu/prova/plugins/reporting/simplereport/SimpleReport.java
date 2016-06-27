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
  private PrintWriter pw;
  private TestRunner testRunner;
  private String outputDirectory;
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
    	outputDirectory = outputDir;
    }
    catch(Exception Ex)
    {
    	
    }
    
  }

  @Override
  public void setUp(String fileName) throws Exception
  {
	  LOGGER.debug("Set up output directory");
	  try
	  {
		  setOutputDir(testRunner.getPropertyValue(Config.PROVA_PLUGINS_REPORTING_DIR));
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
		  File file =new File(outputDirectory+"/"+ fileName + "_" +System.currentTimeMillis()+".html");
    	  if(!file.exists())
    	  {
    		  LOGGER.trace("Creating file: '" + file + "'");
    		  file.createNewFile();
    	  }
    	  FileWriter fw = new FileWriter(file,true);
    	  BufferedWriter bw = new BufferedWriter(fw);
    	  pw = new PrintWriter(bw);
		  pw.println("<!DOCTYPE html>");
		  pw.println("<html>");
		  pw.println("<head>");
		  pw.println("<style> table, td { 	border: 1px solid black;	border-collapse: collapse;	font-family: Verdana, Helvetica, sans-serif;	font-size: 15px;}"
		  							+ "th {	text-align: left;	font-family: Verdana, Helvetica, sans-serif;	font-size: 15px;}"
		  							+ "tr:nth-child(odd) {	background: #CBCDCD;}"
		  							+ "p {	font-family: Verdana, Helvetica, sans-serif;	font-size: 15px;}"
		  							+ "br {	font-family: Verdana, Helvetica, sans-serif;	font-size: 15px;}"
		  							+ "h1 {	font-family: Verdana, Helvetica, sans-serif;	font-size: 30px;}</style>");
		  pw.println("<title>Prova testreport</title>");
		  pw.println("</head>");
		  pw.println("<body>");	  
	  }
	  catch(IOException eX)
	  {
		  LOGGER.debug("Write file has failed; " + eX);
	  }
	  
  }

  @Override
  public void logStartTest(TestCase testCase) throws Exception
  {
	  try
	  {
		  LOGGER.debug("Start SimpleReport Setup");
		  this.setUp(testCase.getId().substring(testCase.getId().lastIndexOf("\\")+1));
		  LOGGER.debug("Wegschrijven begin test");
		  pw.println("<h1>"+testCase.getId().substring(testCase.getId().lastIndexOf("\\")+1)+"</h1>");
		  startTime = System.currentTimeMillis();
		  pw.println("<br><b>Starttime: </b>" + LocalDateTime.now() +"</br>");
		  pw.println("<table>			<tr>				<th>Result</th><th>Action</th><th>Excelrow</th></tr>");
		  
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

	  pw.println("<tr><td style=\"width:200px\" bgcolor=\""+color+"\">"+status+"</td><td style=\"width:1200px\">"+action.toString()+"</td><td style=\"width:200px\">" + action.getId() +"</td></tr>");
    
  }

  @Override
  public void logEndTest(TestCase testCase) throws Exception
  {
	  
	  LOGGER.debug("Status testcase: "+testCase.getStatus());
	  Long elapsedTime = System.currentTimeMillis() - startTime;
	  pw.println("<br><b>Endtime: </b>" + LocalDateTime.now()+"</br>");
	  pw.println("<br><b>Runtime in seconds: </b>" + elapsedTime/1000 + "</br>");
	  if (testCase.getStatus().toString().equalsIgnoreCase("passed"))
	  {
		  pw.println("<br><b>Status testcase: <font color=\"green\">" + testCase.getStatus()+"</b></font></br>" );
	  }
	  else
	  {
		  pw.println("<br><b>Status testcase: <font color=\"red\">" + testCase.getStatus()+"</b></font></br>" );
	  }
	  pw.println("<br><b>Error: </b>" + testCase.getSummary()+"</br>");
	  pw.println("</table>");
	  pw.println("</body>");
	  pw.println("</html>");
	  this.shutDown();
    
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
    	pw.close();
    }
    catch(Exception eX)
    {
    	LOGGER.debug("Writer already has been closed");
    }
    
  }

  @Override
  public void logStartTestSuite(TestSuite testSuite) throws Exception
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void logEndTestSuite(TestSuite testSuite) throws Exception
  {
    // TODO Auto-generated method stub
    
  }
}
