package nl.dictu.prova.framework.web;

import nl.dictu.prova.framework.TestAction;
import nl.dictu.prova.framework.parameters.Text;
import nl.dictu.prova.framework.parameters.TimeOut;
import static nl.dictu.prova.framework.web.StoreText.ATTR_TIMEOUT;

/**
 * Handles the Prova function 'switchScreen' to switch to the next browser
 * screen or pop-up.
 * 
 * @author Coos van der Galiën
 * @since 0.0.1
 */
public class SwitchScreen extends TestAction {

  public Text name;
  public final static String ATTR_NAME  = "NAME";
  
	public SwitchScreen() {
		super();
    name = new Text();
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
		switch(key.trim().toUpperCase())
    {
      case ATTR_PARAMETER :
      case ATTR_NAME      : name.setValue(value);   break;
    }
	}

	@Override
	public void execute() throws Exception {
		LOGGER.trace("> Execute test action: {}", () -> this.toString());

		if (!isValid())
			throw new Exception("Action is not validated!");

		testRunner.getWebActionPlugin().doSwitchScreen(name.getValue());
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
