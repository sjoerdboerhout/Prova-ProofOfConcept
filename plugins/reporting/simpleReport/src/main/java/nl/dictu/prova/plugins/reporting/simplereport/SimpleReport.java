package nl.dictu.prova.plugins.reporting.simplereport;

import nl.dictu.prova.TestRunner;
import nl.dictu.prova.framework.TestAction;
import nl.dictu.prova.framework.TestCase;
import nl.dictu.prova.plugins.reporting.ReportingPlugin;

/*
 * Hello world!
 *
 */
public class SimpleReport implements ReportingPlugin
{
  public static void main(String[] args)
  {
    System.out.println("Hello World!");
  }

  @Override
  public void init(TestRunner testRunner)
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void setOutputDir(String outputDir) throws Exception
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void setUp() throws Exception
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void logStartTest(TestCase testCase) throws Exception
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void logAction(TestAction action) throws Exception
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void logEndTest(TestCase testCase) throws Exception
  {
    // TODO Auto-generated method stub
    
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
    // TODO Auto-generated method stub
    
  }
}
