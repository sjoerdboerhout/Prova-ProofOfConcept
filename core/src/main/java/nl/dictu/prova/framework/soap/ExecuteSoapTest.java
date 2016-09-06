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
class ExecuteSoapTest extends TestAction {

  String property;
  String test;
  String result = null;

  public ExecuteSoapTest() {
  }

  @Override
  public void setAttribute(String key, String value) throws Exception {
      LOGGER.trace("Request to set test '{}' to '{}'", () -> key, () -> value);
      this.property = key;
      this.test = value;
  }

  @Override
  public void execute() throws Exception {
      LOGGER.info("> Execute test action: {}", () -> this.getClass().getSimpleName());

      if(!isValid()){
        throw new Exception("testRunner, property or test not properly set!");
      }

      if(testRunner.getSoapActionPlugin().doTest(property, test)){
        result = "succesful";
      } else {
        result = "unsuccesful";
      }
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
      if(result == null){
        return ("'" + this.getClass().getSimpleName().toUpperCase() + "'");
      }
      return ("'" + this.getClass().getSimpleName().toUpperCase() + "': Value of property '" + property + "' was checked with validation '" + test + "', result is '" + result + "'");
    }
    
}
