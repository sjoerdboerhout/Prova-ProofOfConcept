package nl.dictu.prova.framework;

import java.util.LinkedList;

/**
 * Contains all the data of a test case including a list of all actions that
 * are part of this test.
 * 
 * @author  Sjoerd Boerhout
 * @since   0.0.1
 */
public class TestCase
{
  private LinkedList<TestAction> testActions = new LinkedList<TestAction>();
}
