/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.resources.session;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;

/**
 * Tests performing multiple snapshots on a workspace that has
 * never been saved, then crashing and recovering.
 */
public class TestMultiSnap extends WorkspaceSerializationTest {
	/**
	 * Constructor for TestMultiSnap.
	 */
	public TestMultiSnap() {
		super();
	}

	/**
	 * Constructor for TestMultiSnap.
	 * @param name
	 */
	public TestMultiSnap(String name) {
		super(name);
	}

	public void test1() throws Exception {
		/* create some resource handles */
		IProject project = getWorkspace().getRoot().getProject(PROJECT);
		project.create(getMonitor());
		project.open(getMonitor());

		/* snapshot */
		workspace.save(false, getMonitor());

		/* do more stuff */
		IFolder folder = project.getFolder(FOLDER);
		folder.create(true, true, getMonitor());

		workspace.save(false, getMonitor());

		/* do even more stuff */
		IFile file = folder.getFile(FILE);
		byte[] bytes = "Test bytes".getBytes();
		java.io.ByteArrayInputStream in = new java.io.ByteArrayInputStream(bytes);
		file.create(in, true, getMonitor());

		workspace.save(false, getMonitor());

		//exit without saving
	}

	public void test2() throws CoreException {
		IProject project = getWorkspace().getRoot().getProject(PROJECT);
		IFolder folder = project.getFolder(FOLDER);
		IFile file = folder.getFile(FILE);

		/* see if the workspace contains the resources created earlier*/
		IResource[] children = getWorkspace().getRoot().members();
		assertEquals("1.0", 1, children.length);
		assertEquals("1.1", children[0], project);
		assertTrue("1.2", project.exists());
		assertTrue("1.3", project.isOpen());

		assertExistsInWorkspace("1.4", new IResource[] {project, folder, file});
	}
}