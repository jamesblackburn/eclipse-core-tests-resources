/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.internal.resources;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.internal.resources.ProjectVariant;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.tests.resources.ResourceTest;

public class ProjectVariantsTest extends ResourceTest {
	public static Test suite() {
		return new TestSuite(ProjectVariantsTest.class);
	}

	public ProjectVariantsTest(String name) {
		super(name);
	}

	private IProject project;
	private String variantId0 = "Variant0";
	private String variantId1 = "Variant1";
	private String variantId2 = "Variant2";
	private IProjectVariant variant0;
	private IProjectVariant variant1;
	//private IProjectVariant variant2;
	private IProjectVariant defaultVariant;

	public void setUp() throws Exception {
		project = getWorkspace().getRoot().getProject("Project");
		ensureExistsInWorkspace(new IProject[] {project}, true);
		variant0 = new ProjectVariant(project, variantId0);
		variant1 = new ProjectVariant(project, variantId1);
		defaultVariant = new ProjectVariant(project, IProjectDescription.DEFAULT_VARIANT);
	}

	public void testBasics() throws CoreException {
		IProjectDescription desc = project.getDescription();
		IProjectVariant[] variants = new IProjectVariant[] {variant0, variant1};
		String[] variantIds = new String[] {variantId0, variantId1};
		desc.setVariants(variantIds);
		project.setDescription(desc, getMonitor());

		assertEquals("1.0", variantIds, desc.getVariants());

		assertTrue("2.0", desc.hasVariant(variantId0));
		assertTrue("2.1", desc.hasVariant(variantId1));
		assertFalse("2.2", desc.hasVariant(variantId2));
		assertTrue("2.3", project.hasVariant(variantId0));
		assertTrue("2.4", project.hasVariant(variantId1));
		assertFalse("2.5", project.hasVariant(variantId2));

		assertEquals("3.0", variants[0], project.getActiveVariant());
		project.setActiveVariant(variantId1);
		assertEquals("3.1", variants[1], project.getActiveVariant());
		project.setActiveVariant(variantId2);
		assertEquals("3.2", variants[1], project.getActiveVariant());

		IProjectVariant variant = project.getVariant(variantId0);
		assertEquals("4.0", project, variant.getProject());
		assertEquals("4.1", variantId0, variant.getVariant());
		try {
			project.getVariant(variantId2);
			fail("4.2");
		} catch (CoreException e) {
		}
	}

	public void testNullVariantNamesAreIgnored() throws CoreException {
		IProjectDescription desc = project.getDescription();
		desc.setVariants(new String[] {null});
		project.setDescription(desc, getMonitor());
		assertEquals("1.0", new String[] {IProjectDescription.DEFAULT_VARIANT}, desc.getVariants());
		desc.setVariants(new String[] {variantId0, null, variantId1});
		project.setDescription(desc, getMonitor());
		assertEquals("2.0", new String[] {variantId0, variantId1}, desc.getVariants());
	}

	public void testDuplicates() throws CoreException {
		IProjectDescription desc = project.getDescription();
		desc.setVariants(new String[] {variantId0, variantId1, variantId0});
		project.setDescription(desc, getMonitor());
		assertEquals("1.0", new String[] {variantId0, variantId1}, desc.getVariants());
		desc.setVariants(new String[] {null, variantId1, variantId0, variantId0, null});
		project.setDescription(desc, getMonitor());
		assertEquals("2.0", new String[] {variantId1, variantId0}, desc.getVariants());
	}

	public void testDefaultVariant() throws CoreException {
		IProjectDescription desc = project.getDescription();
		desc.setVariants(new String[] {});
		project.setDescription(desc, getMonitor());

		assertEquals("1.0", new String[] {IProjectDescription.DEFAULT_VARIANT}, desc.getVariants());
		assertTrue("1.1", desc.hasVariant(IProjectDescription.DEFAULT_VARIANT));
		assertTrue("1.2", project.hasVariant(IProjectDescription.DEFAULT_VARIANT));

		assertEquals("2.0", defaultVariant, project.getActiveVariant());
		project.setActiveVariant(IProjectDescription.DEFAULT_VARIANT);
		assertEquals("2.1", defaultVariant, project.getActiveVariant());
	}

	public void testRemoveActiveVariant() throws CoreException {
		IProjectDescription desc = project.getDescription();
		desc.setVariants(new String[0]);
		desc.setVariants(new String[] {variantId0, variantId1});
		project.setDescription(desc, getMonitor());
		assertEquals("1.0", variant0, project.getActiveVariant());
		desc.setVariants(new String[] {variantId0, variantId2});
		project.setDescription(desc, getMonitor());
		assertEquals("2.0", variant0, project.getActiveVariant());
		project.setActiveVariant(variantId2);
		desc.setVariants(new String[] {variantId0, variantId1});
		project.setDescription(desc, getMonitor());
		assertEquals("3.0", variant0, project.getActiveVariant());
	}
}
