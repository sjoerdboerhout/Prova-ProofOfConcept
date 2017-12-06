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
import nl.dictu.prova.framework.ActionFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A factory that allows the input plug-in to create a new web-action.
 * 
 * @author Coos van der Galiën
 * @since 2016-06-27
 */
public class SoapActionFactory implements ActionFactory {

    protected final static Logger LOGGER = LogManager.getLogger();

    public final static String ACTION_SETMESSAGE = "SETSOAPQUERY";
    public final static String ACTION_SETPROPERTIES = "SETSOAPPROPERTIES";
    public final static String ACTION_EXECUTETEST = "EXECUTESOAPTEST";
    public final static String ACTION_PROCESSRESPONSE = "PROCESSSOAPRESPONSE";

    /**
     * Get the corresponding action for <name>
     * 
     * @param name
     * @return
     * @throws Exception
     */
    public TestAction getAction(String name) throws Exception {
        LOGGER.trace("Request to produce webaction '{}'", () -> name);

        switch (name.toUpperCase()) {
        case ACTION_SETMESSAGE:
            return new SetSoapMessage();
        case ACTION_SETPROPERTIES:
            return new SetSoapProperties();
        case ACTION_EXECUTETEST:
            return new ExecuteSoapTest();
        case ACTION_PROCESSRESPONSE:
            return new ProcessSoapResponse();
        }

        throw new Exception("Unknown action '" + name + "' requested");
    }
}
