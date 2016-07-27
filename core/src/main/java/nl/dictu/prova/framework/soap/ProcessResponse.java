package nl.dictu.prova.framework.soap;

import java.util.Map;
import nl.dictu.prova.framework.TestAction;

public class ProcessResponse extends TestAction {

    String currentPrefix;

    @Override
    public void setAttribute(String key, String value) throws Exception {
        
    }

    @Override
    public void execute() throws Exception {
        if(!isValid()){
            throw new Exception("testRunner not properly set!");
        }
        
        //Check if prefix is provided. Set provided or default prefix to be used with property storage.
        currentPrefix = this.testRunner.getSoapActionPlugin().doGetCurrentPrefix();

        //Add all returning properties to global collection
        for (Map.Entry entry : this.testRunner.getSoapActionPlugin().doProcessResponse().entrySet()) {
            this.testRunner.setPropertyValue(currentPrefix + "_" + (String) entry.getKey(), (String) entry.getValue());
            //LOGGER.trace("Added key " + (String) entry.getKey() + " and value " + (String) entry.getValue() + " to properties");
        }
    }

    @Override
    public boolean isValid() throws Exception {
        if (testRunner == null) return false;
        return true;
    }

}
