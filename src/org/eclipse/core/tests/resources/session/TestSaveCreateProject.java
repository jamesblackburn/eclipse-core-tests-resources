/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/
package org.eclipse.core.tests.resources.session;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;

/**
 * Tests recovery after adding a project and not saving
 */
public class TestSaveCreateProject extends WorkspaceSerializationTest {
/**
 * Constructor for TestSaveCreateProject.
 */
public TestSaveCreateProject() {
	super();
}
/**
 * Constructor for TestSaveCreateProject.
 * @param name
 */
public TestSaveCreateProject(String name) {
	super(name);
}
public void test1() {
	/* create some resource handles */
	IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT);
	try {
		workspace.save(true, getMonitor());

		project.create(getMonitor());
		project.open(getMonitor());
	} catch (CoreException e) {
		fail("1.0", e);
	}
}
public void test2() {
	IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
	assertTrue("1.0", root.exists());
	try {
		IResource[] children = root.members();
		assertEquals("1.2", 1, children.length);
		IProject project = (IProject)children[0];
		assertTrue("1.3", project.exists());	
		assertTrue("1.4", !project.isOpen());
		assertEquals("1.5", PROJECT, project.getName());
	} catch (CoreException e) {
		fail("1.99", e);
	}
}
}