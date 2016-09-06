/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.dictu.prova.framework.db;

import nl.dictu.prova.framework.TestAction;

/**
 *
 * @author cimangalienc
 */
class SetDbProperties extends TestAction {
    private String address;
    private String user;
    private String password;
    private String prefix;
    private Boolean rollback;

    @Override
    public void setAttribute(String key, String value) throws Exception {
        LOGGER.trace("Request to set '{}' to '{}'", () -> key, () -> value);
        switch(key){
            case("prova.properties.password"): 
                password = value;
                break;
            case("prova.properties.user"): 
                user = value;
                break;
            case("prova.properties.address"): 
                address = value;
                break;
            case("prova.properties.prefix"):
                prefix = value;
                break;
            case("prova.properties.rollback"): 
                if(value.equals("false")){
                    rollback = false;
                } else {
                    rollback = true;
                };
                break;
            default: LOGGER.error("Attribute not supported.");
        }
    }

    @Override
    public void execute() throws Exception {
        LOGGER.info("> Execute test action: {}", () -> this.getClass().getSimpleName());
        if(!isValid()){
            throw new Exception("Properties not set properly!");
        }
        this.testRunner.getDbActionPlugin().doSetDbProperties(address, user, password, prefix, rollback);
    }

    @Override
    public boolean isValid() throws Exception {
        if(address == null) return false;
        if(user == null) return false;
        if(password == null) return false;
        if(rollback == null) return false;
        return true;
    }
    
    /**
     * Return a string representation of the objects content
     *
     * @return
     */
    @Override
    public String toString() {
        return ("'" + this.getClass().getSimpleName().toUpperCase() + "': '" + address + "', '" + user + "', '" + password + "', '" + rollback + "'");
    }
    
}
