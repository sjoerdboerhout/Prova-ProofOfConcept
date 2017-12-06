/**
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * <p>
 * http://ec.europa.eu/idabc/eupl
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * <p>
 * Date:      18-12-2016
 * Author(s): Sjoerd Boerhout, Robert Bralts & Coos van der Galiën
 * <p>
 */
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
