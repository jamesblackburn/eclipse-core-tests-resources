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
 * This class tests the persistence of the project description Dynamic data
 * that is persisted in the workspace metadata.
 */
public class ProjectDescriptionDynamicTest extends WorkspaceSessionTest {

	IProject proj;
	IBuildConfiguration config;
	IProject[] dynRefs;
	IBuildConfigReference[] configRefs;

	public ProjectDescriptionDynamicTest() {
		super();
	}

	protected void setUp() throws Exception {
		IWorkspaceRoot wr = ResourcesPlugin.getWorkspace().getRoot();
		proj = wr.getProject("referencing");
		config = proj.newBuildConfiguration("someConfiguration");
		dynRefs = new IProject[] {wr.getProject("ref1"), wr.getProject("ref2")};

		//		configRefs = new IBuildConfigReference[] {wr.getProject("ref3").newReference()("ref3config1"), wr.getProject("ref2").newBuildConfiguration("ref2config1"), wr.getProject("ref1").newBuildConfiguration("ref1config1")};
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
		desc.setDynamicReferences(dynRefs);
		proj.setDescription(desc, getMonitor());

		ResourcesPlugin.getWorkspace().save(true, getMonitor());
	}

	/**
	 * Check the project level references still exist
	 */
	public void test2() throws Exception {
		assertTrue("1.0", proj.isAccessible());
		assertEquals("1.1", proj.getDescription().getDynamicReferences(), dynRefs);

		// set build configuration level references on the project
		IProjectDescription desc = proj.getDescription();
		desc.setBuildConfigurations(new IBui))
		desc.setDynamicConfigReferences(configId, references)

		ResourcesPlugin.getWorkspace().save(true, getMonitor());
	}

	/**
	 * Check that the dynamic build configuration references still exist
	 */
	public void test3() throws Exception {

	}

	public static Test suite() {
		return new WorkspaceSessionTestSuite(AutomatedTests.PI_RESOURCES_TESTS, ProjectDescriptionDynamicTest.class);
	}

}
