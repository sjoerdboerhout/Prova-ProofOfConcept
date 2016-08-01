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
class SetQuery extends TestAction {

    private String query;

    @Override
    public void setAttribute(String key, String value) throws Exception {
        switch (key) {
            case ("prova.properties.query"):
                query = value;
                LOGGER.trace("Setting attribute query.");
                break;
            default:
                LOGGER.error("Attribute not supported!");
        }
    }

    @Override
    public void execute() throws Exception {
        LOGGER.trace("Executing testAction setQuery");
        if (!isValid()) {
            throw new Exception("testRunner and/or query are not properly set!");
        }
        this.testRunner.getDbActionPlugin().doSetQuery(query);
    }

    @Override
    public boolean isValid() throws Exception {
        if (testRunner == null) {
            return false;
        }
        if (query == null) {
            return false;
        }
        return true;
    }

    /**
     * Return a string representation of the objects content
     *
     * @return
     */
    @Override
    public String toString() {
        return ("'" + this.getClass().getSimpleName().toUpperCase() + "': " + query.substring(0, 30) + "... '");
    }

}
