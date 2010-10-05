/*******************************************************************************
 * Copyright (c) 2010 Broadcom Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Broadcom Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.internal.resources;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.internal.resources.BuildConfigReference;
import org.eclipse.core.internal.resources.BuildConfiguration;
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
	private IBuildConfiguration project0v0;
	private IBuildConfiguration project0v1;
	private IBuildConfiguration project1v0;
	private IBuildConfiguration project1v1;
	private IBuildConfiguration project2v0;
	private IBuildConfiguration project2v1;
	private IBuildConfiguration project3v0;
	private IBuildConfiguration project3v1;
	private IBuildConfigReference project0v0r;
	private IBuildConfigReference project0v1r;
	private IBuildConfigReference project1ActiveR;
	private IBuildConfigReference project1v0r;
	private IBuildConfigReference project1v1r;
	private IBuildConfigReference project2ActiveR;
	private IBuildConfigReference project2v0r;
	private IBuildConfigReference project3ActiveR;
	private String variant0 = "Variant0";
	private String variant1 = "Variant1";
	private String nonExistantVariant = "foo";

	public ProjectReferencesTest(String name) {
		super(name);
	}

	public void setUp() throws Exception {
		project0 = getWorkspace().getRoot().getProject("ProjectReferencesTest_p0");
		project1 = getWorkspace().getRoot().getProject("ProjectReferencesTest_p1");
		project2 = getWorkspace().getRoot().getProject("ProjectReferencesTest_p2");
		project3 = getWorkspace().getRoot().getProject("ProjectReferencesTest_p3");
		ensureExistsInWorkspace(new IProject[] {project0, project1, project2, project3}, true);
		setUpVariants(project0);
		setUpVariants(project1);
		setUpVariants(project2);
		setUpVariants(project3);
		project0v0 = new BuildConfiguration(project0, variant0);
		project0v1 = new BuildConfiguration(project0, variant1);
		project1v0 = new BuildConfiguration(project1, variant0);
		project1v1 = new BuildConfiguration(project1, variant1);
		project2v0 = new BuildConfiguration(project2, variant0);
		project2v1 = new BuildConfiguration(project2, variant1);
		project3v0 = new BuildConfiguration(project3, variant0);
		project3v1 = new BuildConfiguration(project3, variant1);
		project0v0r = new BuildConfigReference(project0v0);
		project0v1r = new BuildConfigReference(project0v1);
		project1ActiveR = new BuildConfigReference(project1);
		project1v0r = new BuildConfigReference(project1v0);
		project1v1r = new BuildConfigReference(project1v1);
		project2ActiveR = new BuildConfigReference(project2);
		project2v0r = new BuildConfigReference(project2v0);
		project3ActiveR = new BuildConfigReference(project3);
	}

	protected void tearDown() throws Exception {
		super.tearDown();

		// clean-up resources
		project0.delete(true, null);
		project1.delete(true, null);
		project2.delete(true, null);
		project3.delete(true, null);
	}

	private void setUpVariants(IProject project) throws CoreException {
		IProjectDescription desc = project.getDescription();
		desc.setBuildConfigurations(new IBuildConfiguration[] {desc.newBuildConfiguration(variant0), desc.newBuildConfiguration(variant1)});
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
		assertEquals("1.2", new IBuildConfigReference[] {project3ActiveR, project1ActiveR}, desc.getReferencedProjectConfigs(variant0));
		assertEquals("1.3", new IBuildConfigReference[] {project1ActiveR, project2ActiveR}, desc.getDynamicConfigReferences(variant0));

		assertEquals("2.0", new IProject[] {project3, project1, project2}, project0.getReferencedProjects());
		assertEquals("2.1", new IProject[] {project1, project3}, project0.getReferencingProjects());
		assertEquals("2.2", new IBuildConfiguration[] {project3v0, project1v0, project2v0}, project0.getReferencedBuildConfigurations(project0v0));
		assertEquals("2.3", new IBuildConfiguration[] {project1v0, project1v1, project3v0, project3v1}, project0.getReferencingBuildConfigurations(project0v0));
	}

	public void testSetAndGetProjectVariantReferences() throws CoreException {
		// Set project variant references
		IProjectDescription desc = project0.getDescription();
		desc.setReferencedProjectConfigs(variant0, new IBuildConfigReference[] {project1v0r});
		desc.setDynamicConfigReferences(variant0, new IBuildConfigReference[] {project2v0r, project1v0r});
		desc.setReferencedProjectConfigs(variant1, new IBuildConfigReference[] {project1v1r});
		desc.setDynamicConfigReferences(variant1, new IBuildConfigReference[] {project2v0r});
		project0.setDescription(desc, getMonitor());

		desc = project1.getDescription();
		desc.setReferencedProjectConfigs(variant0, new IBuildConfigReference[] {project0v0r});
		desc.setDynamicConfigReferences(variant0, new IBuildConfigReference[] {project0v1r});
		desc.setReferencedProjectConfigs(variant1, new IBuildConfigReference[] {});
		desc.setDynamicConfigReferences(variant1, new IBuildConfigReference[] {});
		project1.setDescription(desc, getMonitor());

		desc = project2.getDescription();
		desc.setReferencedProjectConfigs(variant0, new IBuildConfigReference[] {});
		desc.setDynamicConfigReferences(variant0, new IBuildConfigReference[] {});
		desc.setReferencedProjectConfigs(variant1, new IBuildConfigReference[] {project0v0r});
		desc.setDynamicConfigReferences(variant1, new IBuildConfigReference[] {});
		project2.setDescription(desc, getMonitor());

		desc = project3.getDescription();
		desc.setReferencedProjectConfigs(variant0, new IBuildConfigReference[] {});
		desc.setDynamicConfigReferences(variant0, new IBuildConfigReference[] {project0v1r});
		desc.setReferencedProjectConfigs(variant1, new IBuildConfigReference[] {});
		desc.setDynamicConfigReferences(variant1, new IBuildConfigReference[] {});
		project3.setDescription(desc, getMonitor());

		// Check getters
		desc = project0.getDescription();
		assertEquals("1.0", new IProject[] {project1}, desc.getReferencedProjects());
		assertEquals("1.1", new IProject[] {project2, project1}, desc.getDynamicReferences());
		assertEquals("1.2", new IBuildConfigReference[] {project1v0r}, desc.getReferencedProjectConfigs(variant0));
		assertEquals("1.3", new IBuildConfigReference[] {project2v0r, project1v0r}, desc.getDynamicConfigReferences(variant0));
		assertEquals("1.4", new IBuildConfigReference[] {project1v1r}, desc.getReferencedProjectConfigs(variant1));
		assertEquals("1.5", new IBuildConfigReference[] {project2v0r}, desc.getDynamicConfigReferences(variant1));

		assertEquals("2.0", new IProject[] {project1, project2}, project0.getReferencedProjects());
		assertEquals("2.1", new IProject[] {project1, project2, project3}, project0.getReferencingProjects());
		assertEquals("2.2", new IBuildConfiguration[] {project1v0, project2v0}, project0.getReferencedBuildConfigurations(project0v0));
		assertEquals("2.3", new IBuildConfiguration[] {project1v1, project2v0}, project0.getReferencedBuildConfigurations(project0v1));
		assertEquals("2.4", new IBuildConfiguration[] {project1v0, project2v1}, project0.getReferencingBuildConfigurations(project0v0));
		assertEquals("2.5", new IBuildConfiguration[] {project1v0, project3v0}, project0.getReferencingBuildConfigurations(project0v1));
	}

	public void testAddReferencesToNonExistantVariant() throws CoreException {
		IProjectDescription desc = project0.getDescription();

		assertFalse("1.0", project0.hasBuildConfiguration(desc.newBuildConfiguration(nonExistantVariant)));

		desc.setReferencedProjectConfigs(nonExistantVariant, new IBuildConfigReference[] {project1v0r});
		desc.setDynamicConfigReferences(nonExistantVariant, new IBuildConfigReference[] {project1v0r});
		project0.setDescription(desc, getMonitor());

		assertFalse("2.0", project0.hasBuildConfiguration(desc.newBuildConfiguration(nonExistantVariant)));

		assertEquals("3.0", new IBuildConfigReference[0], desc.getReferencedProjectConfigs(nonExistantVariant));
		assertEquals("3.1", new IBuildConfigReference[0], desc.getDynamicConfigReferences(nonExistantVariant));
		try {
			project0.getReferencedBuildConfigurations(desc.newBuildConfiguration(nonExistantVariant));
			fail("3.2");
		} catch (CoreException e) {
		}
	}

	public void testReferencesToActiveVariants() throws CoreException {
		IProjectDescription desc = project0.getDescription();
		desc.setReferencedProjectConfigs(variant0, new IBuildConfigReference[] {project1.newReference()});
		project0.setDescription(desc, getMonitor());

		assertEquals("1.0", new IBuildConfigReference[] {new BuildConfigReference(project1)}, desc.getReferencedProjectConfigs(variant0));
		assertEquals("1.0", new IBuildConfiguration[] {project1v0}, project0.getReferencedBuildConfigurations(project0v0));
	}
}
