/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.dictu.prova.framework.db;

import nl.dictu.prova.framework.TestAction;

/**
 *
 * @author Coos van der Galiën
 */
public class SetDbPollProperties extends TestAction
{

  private Integer waittime;
  private Integer retries;
  private String result;

  @Override
  public void setAttribute(String key, String value) throws Exception
  {
    LOGGER.trace("Request to set '{}' to '{}'", () -> key, () -> value);
    switch (key)
    {
      case ("prova.properties.waittime"):
        waittime = Integer.parseInt(value);
        break;
      case ("prova.properties.retries"):
        retries = Integer.parseInt(value);
        break;
      case ("prova.properties.result"):
        result = value.trim();
        break;
      default:
        LOGGER.error("Attribute not supported.");
    }
  }

  @Override
  public void execute() throws Exception
  {
    LOGGER.info("> Execute test action: {}", () -> this.getClass().getSimpleName());
    if (!isValid())
    {
      throw new Exception("Poll properties not set properly!");
    }
    this.testRunner.getDbActionPlugin().doSetDbPollProperties(waittime, retries, result);
  }

  @Override
  public boolean isValid() throws Exception
  {
    if (retries == null)
    {
      return false;
    }
    if (waittime == null)
    {
      return false;
    }
    if (result == null)
    {
      return false;
    }
    return true;
  }

  /**
   * Return a string representation of the objects content
   *
   * @return
   */
  @Override
  public String toString()
  {
    return ("'" + this.getClass().getSimpleName().toUpperCase() + "': '" + retries + "', '" + waittime + "', '" + result + "'");
  }

}
