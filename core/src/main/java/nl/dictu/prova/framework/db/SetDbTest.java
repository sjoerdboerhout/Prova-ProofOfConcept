/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.dictu.prova.framework.db;

import java.util.Properties;
import nl.dictu.prova.framework.TestAction;

/**
 *
 * @author Coos van der Galiën
 */
class SetDbTest extends TestAction {
    
    String property;
    String test;

    @Override
    public void setAttribute(String key, String value) throws Exception {
        LOGGER.trace("Request to set test '{}' to '{}'", () -> key, () -> value);
        switch(key.trim().toUpperCase()){
            case("PROPERTY"): this.property = value;
                break;
            case("TEST"): this.test = value;
                break;
            default: throw new Exception("Only 'Property' or 'Test' attributes are supported");
        }
    }

    @Override
    public void execute() throws Exception {
        LOGGER.info("> Execute test action: {}", () -> this.getClass().getSimpleName());
        
        if(!isValid()){
            throw new Exception("testRunner, property or test not properly set!");
        }
        
        testRunner.getDbActionPlugin().doTest(property, test);
    }

    @Override
    public boolean isValid() throws Exception {
        if(testRunner == null) return false;
        if(property == null) return false;
        if(test == null) return false;
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
