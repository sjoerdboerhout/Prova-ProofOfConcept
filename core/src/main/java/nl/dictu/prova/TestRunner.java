package nl.dictu.prova;

import java.util.ArrayList;

import nl.dictu.prova.framework.TestSuite;
import nl.dictu.prova.plugins.input.InputPlugin;
import nl.dictu.prova.plugins.output.ShellOutputPlugin;
import nl.dictu.prova.plugins.output.WebOutputPlugin;
import nl.dictu.prova.plugins.output.SoapOutputPlugin;
import nl.dictu.prova.plugins.reporting.ReportingPlugin;

/**
 * Describes the functions that must be available for the other parts of the 
 * framework to run.
 * 
 * @author  Sjoerd Boerhout
 * @since   2016-04-06
 */
public interface TestRunner
{
  public Boolean                    hasPropertyValue(String key);
  public String                     getPropertyValue(String key) throws Exception;
  public void                       setPropertyValue(String key, String value) throws Exception;
  public void                       printAllProperties() throws Exception;
  
  public void                       setRootTestSuite(TestSuite testSuite);

  public InputPlugin                getInputPlugin();
  public WebOutputPlugin            getWebActionPlugin();
  public SoapOutputPlugin           getSoapActionPlugin();
  public ShellOutputPlugin          getShellActionPlugin();
  public ArrayList<ReportingPlugin> getReportingPlugins();
}
