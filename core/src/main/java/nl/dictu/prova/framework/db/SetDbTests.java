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
class SetDbTests extends TestAction {

    @Override
    public void setAttribute(String key, String value) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void execute() throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isValid() throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
