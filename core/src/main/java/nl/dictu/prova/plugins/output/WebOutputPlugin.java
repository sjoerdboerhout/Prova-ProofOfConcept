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
package nl.dictu.prova.plugins.output;

import nl.dictu.prova.framework.parameters.Xpath;

public interface WebOutputPlugin extends OutputPlugin {
	public void doCaptureScreen(String fileName) throws Exception;

	public void doClick(String xPath, Boolean rightClick, Integer numberOfClicks, Boolean waitUntilPageLoaded, Boolean continueOnNotFound ) throws Exception;

	public void doDownloadFile(String url, String saveAs) throws Exception;

	public void doSelect(String xPath, Boolean select) throws Exception;
	
	public void doSelectDropdown(String value, String value2) throws Exception;

	public void doSendKeys(String xPath, String keys) throws Exception;

	public void doSetText(String xPath, String text, Boolean replace) throws Exception;

	public void doSleep(long waitTime) throws Exception;
  
    public void doStoreText(String xPath, String regex, String inputtext, String name, double timeout) throws Exception;
	
	public void doSwitchFrame(String xPath, Boolean alert, Boolean accept, String username, String password) throws Exception;

	public void doSwitchScreen(String name) throws Exception;

	public void doValidateElement(String xPath, Boolean exists, double timeOut) throws Exception;

	public void doValidateText(String xPath, String value, Boolean exists, double timeOut) throws Exception;

	public void doNavigate(String string);

	public void doWaitForElement(String xPath, String type, Boolean exists, double timeOut) throws Exception;
}