/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.dictu.prova.framework.db;

import java.util.Map.Entry;
import java.util.Properties;
import nl.dictu.prova.framework.TestAction;

/**
 *
 * @author cimangalienc
 */
class ProcessDbResponse extends TestAction {

    @Override
    public void setAttribute(String key, String value) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void execute() throws Exception {
        LOGGER.info("> Execute test action: {}", () -> this.getClass().getSimpleName());
        if(!isValid())
            throw new Exception("TestRunner is not set.");
        Properties response = this.testRunner.getDbActionPlugin().doProcessDbResponse();
        
        if(!response.isEmpty()){
            for(Entry entry : response.entrySet()){
                if(entry.getKey() != null)
                    this.testRunner.setPropertyValue((String) entry.getKey(), (String) entry.getValue());
            }
        }
    }

    @Override
    public boolean isValid() throws Exception {
        if(this.testRunner == null) return false;
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
