package nl.dictu.prova.framework.web;

import nl.dictu.prova.framework.TestAction;

public class SwitchScreen extends TestAction {

	@Override
	public void setAttribute(String key, String value) throws Exception {
		LOGGER.trace("Attribute usage not implemented for SwitchScreen()");
	}

	@Override
	public void execute() throws Exception {
	    LOGGER.trace("> Execute test action: {}", () -> this.toString());
	    
	    if(!isValid())
	      throw new Exception("Action is not validated!");
	    
	    testRunner.getWebActionPlugin().doSwitchScreen();
		
	}

	  @Override
	  public boolean isValid()
	  {
	    if(testRunner == null)  return false;
	    
	    return true;
	  }

}
