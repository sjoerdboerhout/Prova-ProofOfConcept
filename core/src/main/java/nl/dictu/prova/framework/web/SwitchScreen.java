package nl.dictu.prova.framework.web;

import nl.dictu.prova.framework.TestAction;

/**
 * Handles the Prova function 'switchScreen' to switch to the next browser
 * screen or pop-up.
 * 
 * @author Coos van der Galiën
 * @since 0.0.1
 */
public class SwitchScreen extends TestAction {
  
  private String name = null;

	public SwitchScreen() {
		super();
	}

	/**
	 * Set attribute <key> with <value> - Unknown attributes are ignored -
	 * Accepted attributes are: 'parameter' and 'text' Invalid values result in
	 * an exception
	 * 
	 * @param key
	 * @param value
	 * @throws Exception
	 */
	@Override
	public void setAttribute(String key, String value) throws Exception {
		switch(key.toUpperCase())
    {
      case ATTR_PARAMETER: name = value;
      break;
      default: LOGGER.warn("Only 'PARAMETER' is a valid attribute for SwitchScreen.");
    }
	}

	@Override
	public void execute() throws Exception {
		LOGGER.trace("> Execute test action: {}", () -> this.toString());

		if (!isValid())
			throw new Exception("Action is not validated!");

		testRunner.getWebActionPlugin().doSwitchScreen(name);
	}

	@Override
	public boolean isValid() {
		if (testRunner == null)
			return false;

		return true;
	}

	/**
	 * Return a string representation of the objects content
	 * 
	 * @return
	 */
	@Override
	public String toString() {
		return ("'" + this.getClass().getSimpleName().toUpperCase() + "'");
	}
}
