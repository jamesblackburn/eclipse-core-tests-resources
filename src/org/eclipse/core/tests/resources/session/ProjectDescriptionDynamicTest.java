/*******************************************************************************
 * Copyright (c) 2010 Broadcom Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Broadcom Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.core.tests.resources.session;

import junit.framework.Test;
import org.eclipse.core.resources.*;
import org.eclipse.core.tests.resources.AutomatedTests;
import org.eclipse.core.tests.resources.WorkspaceSessionTest;
import org.eclipse.core.tests.session.WorkspaceSessionTestSuite;

/**
 * This class tests persistence of project description build configurations
 * and project description dynamic state across workbench sessions.
 */
public class ProjectDescriptionDynamicTest extends WorkspaceSessionTest {

	IProject proj;
	IBuildConfiguration[] configs;
	IProject[] dynRefs;
	IBuildConfiguration[] configRefs;
	IProject[] configRefsProjects;

	public ProjectDescriptionDynamicTest() {
		super();
	}

	/**
	 * return reference to the active configuration in the project
	 */
	public IBuildConfiguration getRef(IProject project) {
		return getWorkspace().newBuildConfiguration(project.getName(), null, null);
	}

	/**
	 * return reference to the active configuration in the project
	 */
	public IBuildConfiguration getConfig(IProject project, String id) {
		return getWorkspace().newBuildConfiguration(project.getName(), id, null);
	}

	protected void setUp() throws Exception {
		IWorkspaceRoot wr = getWorkspace().getRoot();
		// The project we're setting metadata on
		proj = wr.getProject("referencing");
		configs = new IBuildConfiguration[] {getConfig(proj, "someConfiguration"), getConfig(proj, "someConfiguration2")};

		// The references:
		// Dynamic Project level
		dynRefs = new IProject[] {wr.getProject("ref1"), wr.getProject("ref2")};
		// Dynamic Build Configuration level -- reverse order
		configRefs = new IBuildConfiguration[] {getWorkspace().newBuildConfiguration("ref3", "ref3config1", null), getWorkspace().newBuildConfiguration("ref2", "ref2config1", null), getWorkspace().newBuildConfiguration("ref1", "ref1config1", null)};
		configRefsProjects = new IProject[] {wr.getProject("ref3"), wr.getProject("ref2"), wr.getProject("ref1")};
		super.setUp();
	}

	public ProjectDescriptionDynamicTest(String name) {
		super(name);
	}

	/**
	 * Create some dynamic project level references
	 */
	public void test1() throws Exception {
		// Projects to references -- needn't exist
		proj.create(getMonitor());
		proj.open(getMonitor());

		IProjectDescription desc = proj.getDescription();
		desc.setBuildConfigurations(configs);
		desc.setDynamicReferences(dynRefs);
		proj.setDescription(desc, getMonitor());

		ResourcesPlugin.getWorkspace().save(true, getMonitor());
	}

	/**
	 * Check that the following still exist:
	 *  - project build configurations
	 *  - project level references still exist
	 */
	public void test2() throws Exception {
		assertTrue("1.0", proj.isAccessible());
		assertEquals("1.1", dynRefs, proj.getDescription().getDynamicReferences());
		assertEquals("1.2", configs, proj.getBuildConfigurations());
		assertEquals("1.3", configs[0], proj.getActiveBuildConfiguration());

		// set build configuration level dynamic references on the project
		IProjectDescription desc = proj.getDescription();
		desc.setBuildConfigReferences(configs[1].getId(), configRefs);
		// Change the active configuration
		desc.setActiveBuildConfiguration(configs[1].getId());
		proj.setDescription(desc, getMonitor());

		ResourcesPlugin.getWorkspace().save(true, getMonitor());
	}

	/**
	 * Check that the following still exist:
	 *  - Active configuration has changed
	 *  - Dynamic project references are correct
	 *  - Build config references are correct
	 */
	public void test3() throws Exception {
		assertTrue("2.0", proj.isAccessible());
		assertEquals("2.1", configs[1], proj.getActiveBuildConfiguration());
		// At description dynamic refs are what was set
		assertEquals("2.2", dynRefs, proj.getDescription().getDynamicReferences());
		// At project all references are union of build configuration and project references		
		assertEquals("2.4", configRefsProjects, proj.getReferencedProjects());

		// At the description level, dynamic config references match what was set.
		assertEquals("2.5", configRefs, proj.getDescription().getBuildConfigReferences(configs[1].getId()));
		// At the project level, references are the union of project and build configuration level references
		IBuildConfiguration[] refs = new IBuildConfiguration[] {configRefs[0], configRefs[1], configRefs[2], getRef(dynRefs[0]), getRef(dynRefs[1])};
		assertEquals("2.6", refs, proj.getReferencedBuildConfigurations(configs[1], true));
		// No other projects exist, so check references are empty if we want to filter empty projects
		assertEquals("2.7", new IBuildConfiguration[0], proj.getReferencedBuildConfigurations(configs[1], false));
	}

	public static Test suite() {
		return new WorkspaceSessionTestSuite(AutomatedTests.PI_RESOURCES_TESTS, ProjectDescriptionDynamicTest.class);
	}

}
