/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.dictu.prova.framework;

import nl.dictu.prova.framework.TestAction;

/**
 *
 * @author CGALIEN
 */
public interface ActionFactory {
    
    public TestAction getAction(String name) throws Exception;
    
}
