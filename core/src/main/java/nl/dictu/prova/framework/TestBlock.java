package nl.dictu.prova.framework;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Marius de Vink
 * 
 *         TestBlock is a placeholder class to group a block of TestActions
 *         together. It allows a group of TestActions to be executed or not
 *         together as a group.
 *
 */
public class TestBlock {

	private LinkedList<TestAction> testActions = new LinkedList<TestAction>();
	private String name = null;
	private String description = null;
	private TestStatus status = TestStatus.NOTRUN;

	public TestBlock(String name) {
		super();
		this.setName(name);
	}

	public List<TestAction> getTestActions() {
		return testActions;
	}

	public void addTestAction(TestAction testAction) {
		getTestActions().add(testAction);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "TestBlock: " + name;
	}

	public TestStatus getStatus() {
		return status;
	}

	public void setStatus(TestStatus status) {
		this.status = status;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
