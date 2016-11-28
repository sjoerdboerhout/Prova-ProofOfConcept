package nl.dictu.prova.plugins.output.shell.shellcommand;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import nl.dictu.prova.TestRunner;
import nl.dictu.prova.framework.TestCase;
import nl.dictu.prova.plugins.output.ShellOutputPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ShellCommand implements ShellOutputPlugin
{
  
  final static Logger LOGGER = LogManager.getLogger();
  
  private TestRunner testRunner;

  @Override
  public String getName()
  {
    return "ShellCommand";
  }

  @Override
  public void init(TestRunner tr) throws Exception
  {
    LOGGER.debug("Init: output plugin Soap messaging!");

    if (tr == null)
    {
      throw new Exception("No testRunner supplied!");
    }

    this.testRunner = testRunner;
  }

  @Override
  public void shutDown()
  {
  }

  @Override
  public void setUp(TestCase tc) throws Exception
  {
  }
  
  @Override
  public void doExecute(String string) throws Exception
  {
    try
    {
      StringBuffer sb = new StringBuffer();
      Process p = Runtime.getRuntime().exec(string);
      p.waitFor();

      BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

      String line = "";

      while ((line = reader.readLine())!= null) 
      {
        sb.append(line + "\n");
      }

      LOGGER.info("Command succesfully executed.");
      LOGGER.trace(sb.toString());
    }
    catch(Exception ex)
    {
      LOGGER.error(ex);
    }
  }
  
}
