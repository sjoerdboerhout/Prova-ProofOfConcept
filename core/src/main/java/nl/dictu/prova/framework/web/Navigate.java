package nl.dictu.prova.framework.web;

import nl.dictu.prova.framework.TestAction;
import nl.dictu.prova.framework.parameters.Url;

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

	@Override
	public void setAttribute(String key, String value) throws Exception {
		LOGGER.trace("Request to set '{}' to '{}'", () -> key, () -> value);

		switch (key.toUpperCase().trim()) {
		case ATTR_PARAMETER:
		case ATTR_HYPERLINK:			url.setValue(value);
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
		} catch (Error e) {
			LOGGER.trace("Error!: " + e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public boolean isValid() throws Exception {
		// TODO Auto-generated method stub
		if (url == null)
			return false;

		return true;
	}

}
