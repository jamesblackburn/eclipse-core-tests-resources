
package org.eclipse.core.tests.resources.session;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.tests.harness.WorkspaceSessionTest;
import org.eclipse.core.tests.internal.builders.DeltaVerifierBuilder;

public class Test1GALH44 extends WorkspaceSessionTest {
/**
 * Prepares the environment.  Create some resources and save the workspace.
 */
public void test1() {
	IProject project = getWorkspace().getRoot().getProject("MyProject");
	IProjectDescription description = getWorkspace().newProjectDescription("MyProject");
	ICommand command = description.newCommand();
	command.setBuilderName(DeltaVerifierBuilder.BUILDER_NAME);
	description.setBuildSpec(new ICommand[] { command });
	try {
		project.create(getMonitor());
		project.open(getMonitor());
		project.setDescription(description, getMonitor());
	} catch (CoreException e) {
		fail("1.0", e);
	}

	IFile file = project.getFile("foo.txt");
	try {
		file.create(getRandomContents(), true, getMonitor());
	} catch (CoreException e) {
		fail("2.0", e);
	}

	try {
		getWorkspace().save(true, getMonitor());
	} catch (CoreException e) {
		fail("3.0", e);
	}
}
/**
 * Step 2, edit a file then immediately crash.
 */
public void test2() {
	IProject project = getWorkspace().getRoot().getProject("MyProject");
	IFile file = project.getFile("foo.txt");
	try {
		file.setContents(getRandomContents(), true, true, getMonitor());
	} catch (CoreException e) {
		fail("1.0", e);
	}
	// crash
	System.exit(-1);
}
/**
 * Now immediately try to save after recovering from crash.
 */
public void test3() {
	try {
		getWorkspace().save(true, getMonitor());
	} catch (CoreException e) {
		fail("3.0", e);
	}
	
	//cleanup
	ensureDoesNotExistInWorkspace(getWorkspace().getRoot());
	try {
		getWorkspace().save(true, null);
	} catch(CoreException e) {
		fail("99.99", e);
	}
}	
}
