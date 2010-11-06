/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * James Blackburn (Broadcom Corp.) - initial API and implementation
 *******************************************************************************/
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
	IBuildConfigReference[] configRefs;
	IProject[] configRefsProjects;

	public ProjectDescriptionDynamicTest() {
		super();
	}

	protected void setUp() throws Exception {
		IWorkspaceRoot wr = ResourcesPlugin.getWorkspace().getRoot();
		// The project we're setting metadata on
		proj = wr.getProject("referencing");
		configs = new IBuildConfiguration[] {
				proj.newBuildConfiguration("someConfiguration"),
				proj.newBuildConfiguration("someConfiguration2")};

		// The references:
		// Dynamic Project level
		dynRefs = new IProject[] { wr.getProject("ref1"), wr.getProject("ref2") };
		// Dynamic Build Configuration level -- reverse order
		configRefs = new IBuildConfigReference[] {
				wr.getProject("ref3").newBuildConfigurationReference("ref3config1"),
				wr.getProject("ref2").newBuildConfigurationReference("ref2config1"),
				wr.getProject("ref1").newBuildConfigurationReference("ref1config1") };
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
		desc.setDynamicConfigReferences(configs[0].getConfigurationId(), configRefs);
		// Change the active configuration
		desc.setActiveBuildConfiguration(configs[1].getConfigurationId());
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
		assertEquals("2.1", configRefsProjects,  proj.getDescription().getDynamicReferences());
		assertEquals("2.1", configRefs,  proj.getDescription().getDynamicConfigReferences(configs[0].getConfigurationId()));
	}

	public static Test suite() {
		return new WorkspaceSessionTestSuite(AutomatedTests.PI_RESOURCES_TESTS,
				ProjectDescriptionDynamicTest.class);
	}

}
