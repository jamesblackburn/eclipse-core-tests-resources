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
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.tests.resources.ResourceTest;

/**
 * Test project variant references
 */
public class ProjectReferencesTest extends ResourceTest {
	public static Test suite() {
		return new TestSuite(ProjectReferencesTest.class);
	}

	private IProject project0;
	private IProject project1;
	private IProject project2;
	private IProject project3;
	private String variant0 = "Variant0";
	private String variant1 = "Variant1";

	public ProjectReferencesTest(String name) {
		super(name);
	}

	public void setUp() throws Exception {
		project0 = getWorkspace().getRoot().getProject("Project0");
		project1 = getWorkspace().getRoot().getProject("Project1");
		project2 = getWorkspace().getRoot().getProject("Project2");
		project3 = getWorkspace().getRoot().getProject("Project3");
		setUpVariants(project0);
		setUpVariants(project1);
		setUpVariants(project2);
		setUpVariants(project3);
		ensureExistsInWorkspace(new IProject[] {project0, project1, project2, project3}, true);
	}

	private void setUpVariants(IProject project) throws CoreException {
		IProjectDescription desc = project.getDescription();
		desc.setVariants(new String[] {variant0, variant1});
		project.setDescription(desc, getMonitor());
	}

	public void testSetAndGetProjectReferences() throws CoreException {
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

	public void testSetAndGetProjectVariantReferences() throws CoreException {
		IProjectVariant a = project3.getVariant(variant0);
		IProjectVariant b = project2.getVariant(variant1);
		IProjectVariant c = project1.getVariant(variant0);
		IProjectVariant d = project3.getVariant(variant1);
		IProjectVariant e = project1.getVariant(variant0);
		IProjectVariant f = project2.getVariant(variant0);
		IProjectDescription desc = project0.getDescription();
		desc.setReferencedProjectVariants(variant0, new IProjectVariant[] {a, b});
		desc.setDynamicVariantReferences(variant0, new IProjectVariant[] {c});
		desc.setReferencedProjectVariants(variant1, new IProjectVariant[] {d, e});
		desc.setDynamicVariantReferences(variant1, new IProjectVariant[] {f});
		project0.setDescription(desc, getMonitor());
		assertArraysEqual(new IProjectVariant[] {a, b, c}, project0.getReferencedProjectVariants(variant0));
		assertArraysEqual(new IProjectVariant[] {d, e, f}, project0.getReferencingProjectVariants(variant1));
	}

	private void assertArraysEqual(Object[] expected, Object[] actual) {
		assertEquals(expected.length, actual.length);
		for (int i = 0; i < expected.length; i++)
			assertEquals(expected[i], actual[i]);
	}
}
