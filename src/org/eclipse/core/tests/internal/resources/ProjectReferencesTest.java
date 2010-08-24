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
import org.eclipse.core.internal.resources.ProjectVariant;
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
	private IProjectVariant project0v0;
	private IProjectVariant project0v1;
	private IProjectVariant project1v0;
	private IProjectVariant project1v1;
	private IProjectVariant project2v0;
	private IProjectVariant project2v1;
	private IProjectVariant project3v0;
	private IProjectVariant project3v1;
	private String variant0 = "Variant0";
	private String variant1 = "Variant1";
	private String nonExistantVariant = "foo";

	public ProjectReferencesTest(String name) {
		super(name);
	}

	public void setUp() throws Exception {
		project0 = getWorkspace().getRoot().getProject("Project0");
		project1 = getWorkspace().getRoot().getProject("Project1");
		project2 = getWorkspace().getRoot().getProject("Project2");
		project3 = getWorkspace().getRoot().getProject("Project3");
		ensureExistsInWorkspace(new IProject[] {project0, project1, project2, project3}, true);
		setUpVariants(project0);
		setUpVariants(project1);
		setUpVariants(project2);
		setUpVariants(project3);
		project0v0 = new ProjectVariant(project0, variant0);
		project0v1 = new ProjectVariant(project0, variant1);
		project1v0 = new ProjectVariant(project1, variant0);
		project1v1 = new ProjectVariant(project1, variant1);
		project2v0 = new ProjectVariant(project2, variant0);
		project2v1 = new ProjectVariant(project2, variant1);
		project3v0 = new ProjectVariant(project3, variant0);
		project3v1 = new ProjectVariant(project3, variant1);
	}

	private void setUpVariants(IProject project) throws CoreException {
		IProjectDescription desc = project.getDescription();
		desc.setVariants(new String[] {variant0, variant1});
		project.setDescription(desc, getMonitor());
	}

	public void testSetAndGetProjectReferences() throws CoreException {
		// Set project references
		IProjectDescription desc = project0.getDescription();
		desc.setReferencedProjects(new IProject[] {project3, project1});
		desc.setDynamicReferences(new IProject[] {project1, project2});
		project0.setDescription(desc, getMonitor());

		desc = project1.getDescription();
		desc.setReferencedProjects(new IProject[] {project0});
		desc.setDynamicReferences(new IProject[] {});
		project1.setDescription(desc, getMonitor());

		desc = project2.getDescription();
		desc.setReferencedProjects(new IProject[] {});
		desc.setDynamicReferences(new IProject[] {});
		project2.setDescription(desc, getMonitor());

		desc = project3.getDescription();
		desc.setReferencedProjects(new IProject[] {});
		desc.setDynamicReferences(new IProject[] {project0});
		project3.setDescription(desc, getMonitor());

		// Test getters
		desc = project0.getDescription();
		assertEquals("1.0", new IProject[] {project3, project1}, desc.getReferencedProjects());
		assertEquals("1.1", new IProject[] {project1, project2}, desc.getDynamicReferences());
		assertEquals("1.2", new IProjectVariant[] {project3v0, project3v1, project1v0, project1v1}, desc.getReferencedProjectVariants(variant0));
		assertEquals("1.3", new IProjectVariant[] {project1v0, project1v1, project2v0, project2v1}, desc.getDynamicVariantReferences(variant0));

		assertEquals("2.0", new IProject[] {project3, project1, project2}, project0.getReferencedProjects());
		assertEquals("2.1", new IProject[] {project1, project3}, project0.getReferencingProjects());
		assertEquals("2.2", new IProjectVariant[] {project3v0, project3v1, project1v0, project1v1, project2v0, project2v1}, project0.getReferencedProjectVariants(variant0));
		assertEquals("2.3", new IProjectVariant[] {project1v0, project1v1, project3v0, project3v1}, project0.getReferencingProjectVariants(variant0));
	}

	public void testSetAndGetProjectVariantReferences() throws CoreException {
		// Set project variant references
		IProjectDescription desc = project0.getDescription();
		desc.setReferencedProjectVariants(variant0, new IProjectVariant[] {project1v0});
		desc.setDynamicVariantReferences(variant0, new IProjectVariant[] {project2v0, project1v0});
		desc.setReferencedProjectVariants(variant1, new IProjectVariant[] {project1v1});
		desc.setDynamicVariantReferences(variant1, new IProjectVariant[] {project2v0});
		project0.setDescription(desc, getMonitor());

		desc = project1.getDescription();
		desc.setReferencedProjectVariants(variant0, new IProjectVariant[] {project0v0});
		desc.setDynamicVariantReferences(variant0, new IProjectVariant[] {project0v1});
		desc.setReferencedProjectVariants(variant1, new IProjectVariant[] {});
		desc.setDynamicVariantReferences(variant1, new IProjectVariant[] {});
		project1.setDescription(desc, getMonitor());

		desc = project2.getDescription();
		desc.setReferencedProjectVariants(variant0, new IProjectVariant[] {});
		desc.setDynamicVariantReferences(variant0, new IProjectVariant[] {});
		desc.setReferencedProjectVariants(variant1, new IProjectVariant[] {project0v0});
		desc.setDynamicVariantReferences(variant1, new IProjectVariant[] {});
		project2.setDescription(desc, getMonitor());

		desc = project3.getDescription();
		desc.setReferencedProjectVariants(variant0, new IProjectVariant[] {});
		desc.setDynamicVariantReferences(variant0, new IProjectVariant[] {project0v1});
		desc.setReferencedProjectVariants(variant1, new IProjectVariant[] {});
		desc.setDynamicVariantReferences(variant1, new IProjectVariant[] {});
		project3.setDescription(desc, getMonitor());

		// Check getters
		desc = project0.getDescription();
		assertEquals("1.0", new IProject[] {project1}, desc.getReferencedProjects());
		assertEquals("1.1", new IProject[] {project2, project1}, desc.getDynamicReferences());
		assertEquals("1.2", new IProjectVariant[] {project1v0}, desc.getReferencedProjectVariants(variant0));
		assertEquals("1.3", new IProjectVariant[] {project2v0, project1v0}, desc.getDynamicVariantReferences(variant0));
		assertEquals("1.4", new IProjectVariant[] {project1v1}, desc.getReferencedProjectVariants(variant1));
		assertEquals("1.5", new IProjectVariant[] {project2v0}, desc.getDynamicVariantReferences(variant1));

		assertEquals("2.0", new IProject[] {project1, project2}, project0.getReferencedProjects());
		assertEquals("2.1", new IProject[] {project1, project2, project3}, project0.getReferencingProjects());
		assertEquals("2.2", new IProjectVariant[] {project1v0, project2v0}, project0.getReferencedProjectVariants(variant0));
		assertEquals("2.3", new IProjectVariant[] {project1v1, project2v0}, project0.getReferencedProjectVariants(variant1));
		assertEquals("2.4", new IProjectVariant[] {project1v0, project2v1}, project0.getReferencingProjectVariants(variant0));
		assertEquals("2.5", new IProjectVariant[] {project1v0, project3v0}, project0.getReferencingProjectVariants(variant1));
	}

	public void testAddReferencesToNonExistantVariant() throws CoreException {
		IProjectDescription desc = project0.getDescription();

		assertFalse("1.0", desc.hasVariant(nonExistantVariant));
		assertFalse("1.1", project0.hasVariant(nonExistantVariant));

		desc.setReferencedProjectVariants(nonExistantVariant, new IProjectVariant[] {project1v0});
		desc.setDynamicVariantReferences(nonExistantVariant, new IProjectVariant[] {project1v0});
		project0.setDescription(desc, getMonitor());

		assertFalse("2.0", desc.hasVariant(nonExistantVariant));
		assertFalse("2.1", project0.hasVariant(nonExistantVariant));

		assertNull("3.0", desc.getReferencedProjectVariants(nonExistantVariant));
		assertNull("3.1", desc.getDynamicVariantReferences(nonExistantVariant));
		assertNull("3.2", project0.getReferencedProjectVariants(nonExistantVariant));
	}

	public void testReferencesToActiveVariants() throws CoreException {
		IProjectDescription desc = project0.getDescription();
		desc.setReferencedProjectVariants(variant0, new IProjectVariant[] {new ProjectVariant(project1)});
		project0.setDescription(desc, getMonitor());

		assertEquals("1.0", new IProjectVariant[] {new ProjectVariant(project1)}, desc.getReferencedProjectVariants(variant0));
		assertEquals("1.0", new IProjectVariant[] {new ProjectVariant(project1)}, project0.getReferencedProjectVariants(variant0));
	}
}
