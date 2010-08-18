/*******************************************************************************
 * Copyright (c) 2010 Broadcom Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Alex Collins (Broadcom Corp.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.internal.resources;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.tests.resources.ResourceTest;

/**
 * Test project variant references
 */
public class ProjectReferencesTest extends ResourceTest {
	public static Test suite() {
		return new TestSuite(ProjectReferencesTest.class);
	}

	public ProjectReferencesTest(String name) {
		super(name);
	}

	public void testSetAndGetProjectReferences() throws CoreException {
		IProject project0 = getWorkspace().getRoot().getProject("Project0");
		IProject project1 = getWorkspace().getRoot().getProject("Project1");
		IProject project2 = getWorkspace().getRoot().getProject("Project2");
		IProject project3 = getWorkspace().getRoot().getProject("Project3");
		ensureExistsInWorkspace(new IProject[] {project0, project1, project2, project3}, true);

		IProjectDescription desc = project0.getDescription();
		desc.setReferencedProjects(new IProject[] {project3, project1});
		desc.setDynamicReferences(new IProject[] {project1, project2});
		project0.setDescription(desc, getMonitor());

		desc = project1.getDescription();
		desc.setReferencedProjects(new IProject[] {project0});
		desc.setDynamicReferences(new IProject[] {});
		project1.setDescription(desc, getMonitor());

		desc = project3.getDescription();
		desc.setReferencedProjects(new IProject[] {});
		desc.setDynamicReferences(new IProject[] {project0});
		project3.setDescription(desc, getMonitor());

		desc = project0.getDescription();
		assertArraysEqual(new IProject[] {project3, project1}, desc.getReferencedProjects());
		assertArraysEqual(new IProject[] {project1, project2}, desc.getDynamicReferences());
		assertArraysEqual(new IProject[] {project3, project1, project2}, project0.getReferencedProjects());
		assertArraysEqual(new IProject[] {project1, project3}, project0.getReferencingProjects());
	}

	private void assertArraysEqual(Object[] expected, Object[] actual) {
		assertEquals(expected.length, actual.length);
		for (int i = 0; i < expected.length; i++)
			assertEquals(expected[i], actual[i]);
	}
}
