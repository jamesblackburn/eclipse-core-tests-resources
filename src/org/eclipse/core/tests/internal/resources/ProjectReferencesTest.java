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
	private IBuildConfiguration project3v0;
	private IBuildConfiguration project3v1;
	private String bc0 = "Variant0";
	private String bc1 = "Variant1";
	private String nonExistentBC = "foo";

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
		project0v0 = new BuildConfiguration(project0, bc0);
		project0v1 = new BuildConfiguration(project0, bc1);
		project1v0 = new BuildConfiguration(project1, bc0);
		project1v1 = new BuildConfiguration(project1, bc1);
		project2v0 = new BuildConfiguration(project2, bc0);
		project3v0 = new BuildConfiguration(project3, bc0);
		project3v1 = new BuildConfiguration(project3, bc1);
	}

	protected void tearDown() throws Exception {
		super.tearDown();

		// clean-up resources
		project0.delete(true, null);
		project1.delete(true, null);
		project2.delete(true, null);
		project3.delete(true, null);
	}

	/**
	 * Returns a reference to the active build configuration
	 * @param project
	 * @return
	 */
	private IBuildConfiguration getRef(IProject project) {
		return new BuildConfiguration(project, null);
	}

	/**
	 * Create 2 build configurations bc0 and bc1 on each project
	 * @param project
	 * @throws CoreException
	 */
	private void setUpVariants(IProject project) throws CoreException {
		IProjectDescription desc = project.getDescription();
		desc.setBuildConfigurations(new IBuildConfiguration[] {getWorkspace().newBuildConfiguration(project.getName(), bc0, null), getWorkspace().newBuildConfiguration(project.getName(), bc1, null)});
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
		assertEquals("1.3", new IBuildConfiguration[] {getRef(project1), getRef(project2)}, desc.getDynamicConfigReferences(bc0));

		assertEquals("2.0", new IProject[] {project3, project1, project2}, project0.getReferencedProjects());
		assertEquals("2.1", new IProject[] {project1, project3}, project0.getReferencingProjects());
		assertEquals("2.2", new IBuildConfiguration[] {project3v0, project1v0, project2v0}, project0.getReferencedBuildConfigurations(project0v0));
		assertEquals("2.3", new IBuildConfiguration[] {project1v0, project1v1, project3v0, project3v1}, project0.getReferencingBuildConfigurations(project0v0));
	}

	public void testSetAndGetProjectVariantReferences() throws CoreException {
		// Set project variant references
		IProjectDescription desc = project0.getDescription();
		// 1 static reference
		desc.setReferencedProjects(new IProject[] {project1});
		// 1 dynamic project-level reference
		desc.setDynamicReferences(new IProject[] {project3});
		// config level references
		desc.setDynamicConfigReferences(bc0, new IBuildConfiguration[] {project2v0, project1v0});
		desc.setDynamicConfigReferences(bc1, new IBuildConfiguration[] {project2v0});
		project0.setDescription(desc, getMonitor());

		desc = project1.getDescription();
		desc.setReferencedProjects(new IProject[] {project0});
		desc.setDynamicConfigReferences(bc0, new IBuildConfiguration[] {project0v1});
		desc.setDynamicConfigReferences(bc1, new IBuildConfiguration[] {});
		project1.setDescription(desc, getMonitor());

		desc = project3.getDescription();
		desc.setDynamicConfigReferences(bc0, new IBuildConfiguration[] {project0v1});
		desc.setDynamicConfigReferences(bc1, new IBuildConfiguration[] {});
		project3.setDescription(desc, getMonitor());

		// Check getters
		desc = project0.getDescription();
		assertEquals("1.0", new IProject[] {project1}, desc.getReferencedProjects());
		assertEquals("1.1", new IProject[] {project2, project1, project3}, desc.getDynamicReferences());
		assertEquals("1.3", new IBuildConfiguration[] {project2v0, project1v0, getRef(project3)}, desc.getDynamicConfigReferences(bc0));
		assertEquals("1.5", new IBuildConfiguration[] {project2v0, getRef(project3)}, desc.getDynamicConfigReferences(bc1));

		assertEquals("2.0", new IProject[] {project2, project1, project3}, project0.getReferencedProjects());
		assertEquals("2.1", new IProject[] {project1, project3}, project0.getReferencingProjects());
		assertEquals("2.2", new IBuildConfiguration[] {project2v0, project1v0, project1.getActiveBuildConfiguration(), project3.getActiveBuildConfiguration()}, project0.getReferencedBuildConfigurations(project0v0));
		assertEquals("2.3", new IBuildConfiguration[] {project2v0, project1.getActiveBuildConfiguration(), project3.getActiveBuildConfiguration()}, project0.getReferencedBuildConfigurations(project0v1));
		assertEquals("2.4", new IBuildConfiguration[] {project1v0, project1v1}, project0.getReferencingBuildConfigurations(project0v0));
		assertEquals("2.5", new IBuildConfiguration[] {project1v0, project3v0}, project0.getReferencingBuildConfigurations(project0v1));
	}

	/**
	 * Tests that setting build configuration level dynamic references
	 * trumps the project level dynamic references when it comes to order.
	 * @throws CoreException
	 */
	public void testMixedProjectAndBuildConfigRefs() throws CoreException {
		// Set project variant references
		IProjectDescription desc = project0.getDescription();
		desc.setDynamicReferences(new IProject[] {project1, project3});
		project0.setDescription(desc, getMonitor());

		// Check getters
		desc = project0.getDescription();
		assertEquals("1.1", new IProject[] {project1, project3}, desc.getDynamicReferences());
		assertEquals("1.2", new IBuildConfiguration[] {getRef(project1), getRef(project3)}, desc.getDynamicConfigReferences(project0v0.getConfigurationId()));
		assertEquals("1.3", new IBuildConfiguration[] {getRef(project1), getRef(project3)}, desc.getDynamicConfigReferences(project0v1.getConfigurationId()));

		// Now set dynamic references on config1
		desc.setDynamicConfigReferences(project0v0.getConfigurationId(), new IBuildConfiguration[] {project3v1, project2v0, project1v0});
		project0.setDescription(desc, getMonitor());

		// Check references
		// This is deterministic as config0 is listed first, so we expect its config order to trump cofig1's
		desc = project0.getDescription();
		assertEquals("2.1", new IProject[] {project3, project2, project1}, desc.getDynamicReferences());
		assertEquals("2.2", new IBuildConfiguration[] {project3v1, project2v0, project1v0, getRef(project1), getRef(project3)}, desc.getDynamicConfigReferences(project0v0.getConfigurationId()));
		assertEquals("2.3", new IBuildConfiguration[] {getRef(project1), getRef(project3)}, desc.getDynamicConfigReferences(project0v1.getConfigurationId()));
	}

	public void testAddReferencesToNonExistantVariant() throws CoreException {
		IProjectDescription desc = project0.getDescription();

		IBuildConfiguration nonExistent = getWorkspace().newBuildConfiguration(project0.getName(), nonExistentBC, null);

		assertFalse("1.0", project0.hasBuildConfiguration(nonExistent));

		desc.setDynamicConfigReferences(nonExistentBC, new IBuildConfiguration[] {project1v0});
		project0.setDescription(desc, getMonitor());

		assertFalse("2.0", project0.hasBuildConfiguration(nonExistent));

		assertEquals("3.1", new IBuildConfiguration[0], desc.getDynamicConfigReferences(nonExistentBC));
		try {
			project0.getReferencedBuildConfigurations(nonExistent);
			fail("3.2");
		} catch (CoreException e) {
		}
	}

	public void testReferencesToActiveVariants() throws CoreException {
		IProjectDescription desc = project0.getDescription();
		desc.setDynamicConfigReferences(bc0, new IBuildConfiguration[] {getRef(project1)});
		project0.setDescription(desc, getMonitor());

		assertEquals("1.0", new IBuildConfiguration[] {getRef(project1)}, desc.getDynamicConfigReferences(bc0));
		assertEquals("1.0", new IBuildConfiguration[] {project1v0}, project0.getReferencedBuildConfigurations(project0v0));
	}
}
