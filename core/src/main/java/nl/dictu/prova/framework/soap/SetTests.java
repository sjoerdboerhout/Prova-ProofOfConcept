/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.dictu.prova.framework.soap;

import nl.dictu.prova.framework.TestAction;

/**
 *
 * @author cimangalienc
 */
class SetTests extends TestAction {

    public SetTests() {
    }

    @Override
    public void setAttribute(String key, String value) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void execute() throws Exception {
        if(!isValid()){
            throw new Exception("testRunner not properly set!");
        }
    }

    @Override
    public boolean isValid() throws Exception {
        if (testRunner == null) return false;
        return true;
    }
    
}
