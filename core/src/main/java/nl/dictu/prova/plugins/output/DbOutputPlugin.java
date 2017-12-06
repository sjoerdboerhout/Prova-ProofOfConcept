/*
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

import java.util.Properties;

/**
 *
 * @author Coos van der Galiën
 */
public interface DbOutputPlugin extends OutputPlugin
{
  public Properties doProcessDbResponse() throws Exception;

  public void doSetQuery(String query) throws Exception;

  public void doSetDbProperties(String adress, String user, String password, String prefix, Boolean rollback) throws Exception;

  public boolean doTest(String property, String test) throws Exception;

  public void doSetDbPollProperties(Integer waittime, Integer retries, String result) throws Exception;
  
  public void doPollForDbResult() throws Exception;
}
