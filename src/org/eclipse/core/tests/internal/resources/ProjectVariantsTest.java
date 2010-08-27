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
	private IProjectVariant variant2;
	private IProjectVariant defaultVariant;

	public void setUp() throws Exception {
		project = getWorkspace().getRoot().getProject("Project");
		ensureExistsInWorkspace(new IProject[] {project}, true);
		variant0 = new ProjectVariant(project, variantId0);
		variant1 = new ProjectVariant(project, variantId1);
		variant2 = new ProjectVariant(project, variantId2);
		defaultVariant = new ProjectVariant(project, IProjectDescription.DEFAULT_VARIANT);
	}

	public void testBasics() throws CoreException {
		IProjectDescription desc = project.getDescription();
		desc.setVariants(new IProjectVariant[] {desc.newVariant(variantId0), desc.newVariant(variantId1)});
		project.setDescription(desc, getMonitor());

		assertEquals("1.0", new IProjectVariant[] {variant0, variant1}, project.getVariants());
		assertEquals("1.1", variant0, project.getVariant(variantId0));
		assertEquals("1.2", variant1, project.getVariant(variantId1));

		assertTrue("2.0", project.hasVariant(variant0));
		assertTrue("2.1", project.hasVariant(variant1));
		assertFalse("2.2", project.hasVariant(variant2));

		assertEquals("3.0", variant0, project.getActiveVariant());
		desc = project.getDescription();
		desc.setActiveVariant(variantId1);
		project.setDescription(desc, getMonitor());
		assertEquals("3.1", variant1, project.getActiveVariant());
		desc = project.getDescription();
		desc.setActiveVariant(variantId2);
		project.setDescription(desc, getMonitor());
		assertEquals("3.2", variant1, project.getActiveVariant());

		IProjectVariant variant = project.getVariants()[0];
		assertEquals("4.0", project, variant.getProject());
		assertEquals("4.1", variantId0, variant.getVariantName());
	}

	public void testDuplicates() throws CoreException {
		IProjectDescription desc = project.getDescription();
		desc.setVariants(new IProjectVariant[] {desc.newVariant(variantId0), desc.newVariant(variantId1), desc.newVariant(variantId0)});
		project.setDescription(desc, getMonitor());
		assertEquals("1.0", new IProjectVariant[] {variant0, variant1}, project.getVariants());
	}

	public void testDefaultVariant() throws CoreException {
		IProjectDescription desc = project.getDescription();
		desc.setVariants(new IProjectVariant[] {});
		project.setDescription(desc, getMonitor());

		assertEquals("1.0", new IProjectVariant[] {defaultVariant}, project.getVariants());
		assertTrue("1.1", project.hasVariant(defaultVariant));

		assertEquals("2.0", defaultVariant, project.getActiveVariant());
		desc = project.getDescription();
		desc.setActiveVariant(IProjectDescription.DEFAULT_VARIANT);
		project.setDescription(desc, getMonitor());
		assertEquals("2.1", defaultVariant, project.getActiveVariant());
	}

	public void testRemoveActiveVariant() throws CoreException {
		IProjectDescription desc = project.getDescription();
		desc.setVariants(new IProjectVariant[0]);
		desc.setVariants(new IProjectVariant[] {variant0, variant1});
		project.setDescription(desc, getMonitor());
		assertEquals("1.0", variant0, project.getActiveVariant());
		desc.setVariants(new IProjectVariant[] {variant0, variant2});
		project.setDescription(desc, getMonitor());
		assertEquals("2.0", variant0, project.getActiveVariant());
		desc = project.getDescription();
		desc.setActiveVariant(variantId2);
		project.setDescription(desc, getMonitor());
		desc.setVariants(new IProjectVariant[] {variant0, variant1});
		project.setDescription(desc, getMonitor());
		assertEquals("3.0", variant0, project.getActiveVariant());
	}

	//	public void testTranslateActiveVariants() throws CoreException {
	//		IProjectVariantManager mngr = getWorkspace().getProjectVariantManager();
	//
	//		IProjectDescription desc = project.getDescription();
	//		desc.setVariants(new String[] {variantId0, variantId1});
	//		project.setDescription(desc, getMonitor());
	//
	//		IProjectVariant[] variants = new IProjectVariant[2];
	//		IProjectVariant[] expectedVariants = new IProjectVariant[2];
	//
	//		// Check that actual variants are unmodified
	//		variants[0] = mngr.createProjectVariant(project, variantId0);
	//		variants[1] = mngr.createProjectVariant(project, variantId1);
	//		expectedVariants[0] = mngr.createProjectVariant(project, variantId0);
	//		expectedVariants[1] = variants[1];
	//		assertEquals("1.0", expectedVariants, mngr.translateActiveVariants(variants));
	//
	//		// Check that active variant is translated
	//		variants[0] = mngr.createProjectVariant(project, null);
	//		variants[1] = mngr.createProjectVariant(project, variantId1);
	//		expectedVariants[0] = mngr.createProjectVariant(project, variantId0);
	//		expectedVariants[1] = variants[1];
	//		assertEquals("2.0", expectedVariants, mngr.translateActiveVariants(variants));
	//
	//		// Check that active variant for inaccessible project is removed
	//		IProject nonexistant = getWorkspace().getRoot().getProject("nonexistant");
	//		assertDoesNotExistInWorkspace(nonexistant);
	//		variants[0] = mngr.createProjectVariant(nonexistant, null);
	//		variants[1] = mngr.createProjectVariant(project, variantId1);
	//		expectedVariants = new IProjectVariant[] {variants[1]};
	//		assertEquals("3.0", expectedVariants, mngr.translateActiveVariants(variants));
	//	}
}
