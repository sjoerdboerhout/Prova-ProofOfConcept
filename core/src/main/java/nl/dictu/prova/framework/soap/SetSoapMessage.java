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
package nl.dictu.prova.framework.soap;

import nl.dictu.prova.framework.TestAction;

public class SetSoapMessage extends TestAction {
    
        private final String ATTR_MESSAGE = "QUERY";
        private String message = null;

	@Override
	public void setAttribute(String key, String value) throws Exception {
        LOGGER.trace("Request to set '{}' to '{}'", () -> key, () -> value);
	    switch(key){
	        case ATTR_MESSAGE:
	            message = value;
	            LOGGER.trace("Setting attribute message.");
	            break;
            default:
                LOGGER.error("Attribute not supported!");
            }
	}

	@Override
	public void execute() throws Exception {
            if(!isValid()){
                throw new Exception("Message or testRunner not properly set!");
            }
            
            this.testRunner.getSoapActionPlugin().doSetMessage(message);
	}

	@Override
	public boolean isValid() throws Exception {
            if (message == null) return false;
            if (testRunner == null) return false;
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
    int length = message.length() < 120 ? message.length() : 120;
    //return ("'" + this.getClass().getSimpleName().toUpperCase() + "': " + message.substring(0, length) + "'");
    return ("'" + this.getClass().getSimpleName().toUpperCase() + "': " + message);
  }
}
