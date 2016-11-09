/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.dictu.prova.framework.web;

import nl.dictu.prova.framework.TestAction;
import nl.dictu.prova.framework.parameters.TimeOut;
import nl.dictu.prova.framework.parameters.Xpath;

/**
 *
 * @author Coos van der Galiën
 */
class StoreText extends TestAction
{
  public final static String ATTR_TIMEOUT  = "TIMEOUT";
  public final static String ATTR_REGEX    = "REGEX";
  public final static String ATTR_NAME     = "NAME";
  public final static String ATTR_XPATH    = "XPATH";
  
  Xpath xPath = null;
  String regex = null;
  String name = null;
  TimeOut timeout;
  
  public StoreText() throws Exception
  {
    timeout = new TimeOut(0); // Ms
    xPath = new Xpath();
  }

  @Override
  public void setAttribute(String key, String value) throws Exception
  {
    switch(key.trim().toUpperCase())
    {
      case ATTR_TIMEOUT   : timeout = new TimeOut(Integer.parseInt(value));   break;
      case ATTR_REGEX     : regex   = value.trim();                           break;
      case ATTR_PARAMETER : name    = value.trim();                           break;
      case ATTR_NAME      : name    = value.trim();                           break;
      case ATTR_XPATH     : xPath.setValue(value);                            break;
      default             : xPath.setAttribute(key, value);
    }
  }

  @Override
  public void execute() throws Exception
  {
    if(!isValid())
    {
      throw new Exception("Please check if xPath, regex, name and timeout values are provided!");
    }
    this.testRunner.getWebActionPlugin().doStoreText(xPath.getValue(), regex, name, new Double(timeout.getValue()));
  }

  @Override
  public boolean isValid() throws Exception
  {
    if(xPath == null) return false;
    if(name == null) return false;
    if(timeout == null) return false;
    return true;
  }
  
}
