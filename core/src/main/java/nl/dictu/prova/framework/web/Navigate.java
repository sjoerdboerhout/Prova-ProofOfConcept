package nl.dictu.prova.framework.web;

import nl.dictu.prova.framework.TestAction;
import nl.dictu.prova.framework.parameters.Url;

/**
 * Handles the Prova function 'navigate' to navigate the current browser to an
 * adress.
 * 
 * @author Coos van der Galiën
 * @since 0.0.1
 */
public class Navigate extends TestAction {
	private static final String ATTR_HYPERLINK = "HYPERLINK";
	private Url url = null;

	/**
	 * Constructor
	 * 
	 * @throws Exception
	 */
	public Navigate() throws Exception {
		super();

		url = new Url();
	}

	/**
	 * Set attribute <key> with <value> - Unknown attributes are ignored -
	 * Invalid values result in an exception
	 * 
	 * @param key
	 * @param value
	 * @throws Exception
	 */
	@Override
	public void setAttribute(String key, String value) throws Exception {
		LOGGER.trace("Request to set '{}' to '{}'", () -> key, () -> value);

		switch (key.toUpperCase().trim()) {
		case ATTR_PARAMETER:
		case ATTR_HYPERLINK:
			url.setValue(value);
			break;
		}
	}

	@Override
	public void execute() throws Exception {
		LOGGER.trace("> Execute test action: {}", () -> this.toString());

		if (!url.isValid())
			throw new Exception("Action is not validated!");

		try {
			testRunner.getWebActionPlugin().doNavigate(url.getValue());
		} catch (Exception eX) {
			LOGGER.trace("Exception!: " + eX.getMessage());
			eX.printStackTrace();
		}
	}

	/**
	 * Check if all requirements are met to execute this action
	 */
	@Override
	public boolean isValid() throws Exception {
		// TODO Auto-generated method stub
		if (url == null)
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
		return ("'" + this.getClass().getSimpleName().toUpperCase()  + "': " + url.getValue() + "'");
	}

}
