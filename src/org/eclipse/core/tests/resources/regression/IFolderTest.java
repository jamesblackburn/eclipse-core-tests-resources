/**********************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0 which
 * accompanies this distribution, and is available at http://www.eclipse.
 * org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.tests.resources.regression;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.boot.BootLoader;
import org.eclipse.core.internal.localstore.CoreFileSystemLibrary;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.tests.harness.EclipseWorkspaceTest;

public class IFolderTest extends EclipseWorkspaceTest {
/**
 * Constructor for IFolderTest.
 */
public IFolderTest() {
	super();
}
/**
 * Constructor for IFolderTest.
 * @param name
 */
public IFolderTest(String name) {
	super(name);
}
public static Test suite() {
	return new TestSuite(IFolderTest.class);
}

/**
 * Bug requests that if a failed folder creation occurs on Linux that we check
 * the immediate parent to see if it is read-only so we can return a better
 * error code and message to the user.
 */
public void testBug25662() {
	
	// We need to know whether or not we can set the parent folder to be read-only
	// in order to perform this test.
	if (!CoreFileSystemLibrary.usingNatives())
		return;
	
	// Only run this test on Linux for now since Windows lets you create
	// a file within a read-only folder.
	if (!BootLoader.getOS().equals(BootLoader.OS_LINUX))
		return;

	IProject project = getWorkspace().getRoot().getProject("MyProject");
	IFolder parentFolder = project.getFolder("parentFolder");
	ensureExistsInWorkspace(new IResource[] {project, parentFolder}, true);
	IFolder folder = parentFolder.getFolder("folder");

	try {
		parentFolder.setReadOnly(true);
		assertTrue("0.0", parentFolder.isReadOnly());
		try {
			folder.create(true, true, getMonitor());
			fail("0.1");
		} catch (CoreException e) {
			assertEquals("0.2", IResourceStatus.PARENT_READ_ONLY, e.getStatus().getCode());
		}
	} finally {
		parentFolder.setReadOnly(false);
	}
}
}

