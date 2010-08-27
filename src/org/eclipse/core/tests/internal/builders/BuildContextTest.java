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
package org.eclipse.core.tests.internal.builders;

import java.util.Arrays;
import java.util.Comparator;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.internal.events.BuildContext;
import org.eclipse.core.internal.resources.ProjectVariantReference;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;

/**
 * These tests exercise the build context functionality that tells a builder in what context
 * it was called.
 */
public class BuildContextTest extends AbstractBuilderTest {
	public static Test suite() {
		return new TestSuite(BuildContextTest.class);
	}

	private IProject project0;
	private IProject project1;
	private IProject project2;
	private IProject project3;
	private IProject project4;
	private final String variant0 = "Variant0";
	private final String variant1 = "Variant1";

	public BuildContextTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		// Create resources
		IWorkspaceRoot root = getWorkspace().getRoot();
		project0 = root.getProject("Project0");
		project1 = root.getProject("Project1");
		project2 = root.getProject("Project2");
		project3 = root.getProject("Project3");
		project4 = root.getProject("Project4");
		IResource[] resources = {project0, project1, project2, project3, project4};
		ensureExistsInWorkspace(resources, true);
		setAutoBuilding(false);
		setupProject(project0);
		setupProject(project1);
		setupProject(project2);
		setupProject(project3);
		setupProject(project4);
	}

	/**
	 * Helper method to configure a project with a build command and several variants.
	 */
	private void setupProject(IProject project) throws CoreException {
		IProjectDescription desc = project.getDescription();

		// Add build command
		ICommand command = createCommand(desc, ContextBuilder.BUILDER_NAME, "Build0");
		command.setBuilding(IncrementalProjectBuilder.AUTO_BUILD, true);
		command.setBuilding(IncrementalProjectBuilder.FULL_BUILD, true);
		command.setBuilding(IncrementalProjectBuilder.INCREMENTAL_BUILD, true);
		command.setBuilding(IncrementalProjectBuilder.CLEAN_BUILD, true);
		desc.setBuildSpec(new ICommand[] {command});

		// Create variants
		desc.setVariants(new IProjectVariant[] {desc.newVariant(variant0), desc.newVariant(variant1)});

		project.setDescription(desc, getMonitor());
	}

	/**
	 * Setup a reference graph, then test the build context for for each project involved
	 * in the 'build'. The reference graph contains the following structures:
	 *  - Multiple directly referenced variants
	 *  - Multiple directly referencing variants
	 *  - Cycles and loops - between both 2 variants and 3 variants
	 *  - Variants that is not part of the build but are part of the reference graph
	 */
	public void testBuildContextForComplexGraph() throws CoreException {
		// Create reference graph
		IProjectVariant p0v0 = project0.getVariant(variant0);
		IProjectVariant p0v1 = project0.getVariant(variant1);
		IProjectVariant p1v0 = project1.getVariant(variant0);
		IProjectVariant p1v1 = project1.getVariant(variant1);
		IProjectVariant p2v0 = project2.getVariant(variant0);
		IProjectVariant p2v1 = project2.getVariant(variant1);
		IProjectVariant p3v0 = project3.getVariant(variant0);
		IProjectVariant p3v1 = project3.getVariant(variant1);
		IProjectVariant p4v0 = project4.getVariant(variant0);
		IProjectVariant p4v1 = project4.getVariant(variant1);
		setReferences(p0v0, new IProjectVariant[] {p0v1, p1v1, p4v0});
		setReferences(p0v1, new IProjectVariant[] {p1v0, p2v0});
		setReferences(p1v0, new IProjectVariant[] {p2v1});
		setReferences(p1v1, new IProjectVariant[] {p2v1, p4v1});
		setReferences(p2v0, new IProjectVariant[] {p3v0});
		setReferences(p2v1, new IProjectVariant[] {});
		setReferences(p3v0, new IProjectVariant[] {p0v1, p2v0, p2v1});
		setReferences(p3v1, new IProjectVariant[] {p0v0, p3v0});
		setReferences(p4v0, new IProjectVariant[] {p0v0, p4v1});
		setReferences(p4v1, new IProjectVariant[] {p1v1});

		// Create build order
		final IProjectVariant[] buildOrder = new IProjectVariant[] {p2v1, p1v0, p1v1, p3v0, p2v0, p0v1, p0v0, p4v1, p4v0};

		IBuildContext context;

		context = new BuildContext(p2v1, buildOrder);
		assertArraysContainSameElements(new IProjectVariant[] {}, context.getAllReferencedProjectVariants());
		assertArraysContainSameElements(new IProjectVariant[] {p1v0, p1v1, p3v0, p2v0, p0v1, p0v0, p4v1, p4v0}, context.getAllReferencingProjectVariants());

		context = new BuildContext(p1v0, buildOrder);
		assertArraysContainSameElements(new IProjectVariant[] {p2v1}, context.getAllReferencedProjectVariants());
		assertArraysContainSameElements(new IProjectVariant[] {p0v0, p0v1, p2v0, p3v0, p4v0}, context.getAllReferencingProjectVariants());

		context = new BuildContext(p1v1, buildOrder);
		assertArraysContainSameElements(new IProjectVariant[] {p2v1}, context.getAllReferencedProjectVariants());
		assertArraysContainSameElements(new IProjectVariant[] {p0v0, p4v0, p4v1}, context.getAllReferencingProjectVariants());

		context = new BuildContext(p3v0, buildOrder);
		assertArraysContainSameElements(new IProjectVariant[] {p2v1}, context.getAllReferencedProjectVariants());
		assertArraysContainSameElements(new IProjectVariant[] {p0v0, p0v1, p2v0, p4v0}, context.getAllReferencingProjectVariants());

		context = new BuildContext(p2v0, buildOrder);
		assertArraysContainSameElements(new IProjectVariant[] {p2v1, p3v0}, context.getAllReferencedProjectVariants());
		assertArraysContainSameElements(new IProjectVariant[] {p0v0, p0v1, p4v0}, context.getAllReferencingProjectVariants());

		context = new BuildContext(p0v1, buildOrder);
		assertArraysContainSameElements(new IProjectVariant[] {p2v1, p1v0, p3v0, p2v0}, context.getAllReferencedProjectVariants());
		assertArraysContainSameElements(new IProjectVariant[] {p0v0, p4v0}, context.getAllReferencingProjectVariants());

		context = new BuildContext(p0v0, buildOrder);
		assertArraysContainSameElements(new IProjectVariant[] {p0v1, p1v0, p1v1, p2v0, p2v1, p3v0}, context.getAllReferencedProjectVariants());
		assertArraysContainSameElements(new IProjectVariant[] {p4v0}, context.getAllReferencingProjectVariants());

		context = new BuildContext(p4v1, buildOrder);
		assertArraysContainSameElements(new IProjectVariant[] {p1v1, p2v1}, context.getAllReferencedProjectVariants());
		assertArraysContainSameElements(new IProjectVariant[] {p4v0}, context.getAllReferencingProjectVariants());

		context = new BuildContext(p4v0, buildOrder);
		assertArraysContainSameElements(new IProjectVariant[] {p2v1, p1v0, p1v1, p3v0, p2v0, p0v1, p0v0, p4v1}, context.getAllReferencedProjectVariants());
		assertArraysContainSameElements(new IProjectVariant[] {}, context.getAllReferencingProjectVariants());
	}

	public void testProjectBuild() throws CoreException {
		setupSimpleReferences();
		ContextBuilder.clearStats();
		project0.build(IncrementalProjectBuilder.FULL_BUILD, getMonitor());
		IBuildContext context = ContextBuilder.getContext(project0.getActiveVariant());
		assertEquals(0, context.getAllReferencedProjects().length);
		assertEquals(0, context.getAllReferencingProjects().length);
		assertEquals(0, context.getAllReferencedProjectVariants().length);
		assertEquals(0, context.getAllReferencingProjectVariants().length);
	}

	public void testWorkspaceBuildProject() throws CoreException {
		setupSimpleReferences();
		ContextBuilder.clearStats();
		getWorkspace().build(new IProjectVariant[] {project0.getActiveVariant()}, IncrementalProjectBuilder.FULL_BUILD, getMonitor());
		IBuildContext context = ContextBuilder.getContext(project0.getActiveVariant());
		assertArraysContainSameElements(new IProject[] {project1, project2}, context.getAllReferencedProjects());
		assertEquals(0, context.getAllReferencingProjects().length);
		context = ContextBuilder.getBuilder(project1.getActiveVariant()).contextForLastBuild;
		assertArraysContainSameElements(new IProject[] {project2}, context.getAllReferencedProjects());
		assertArraysContainSameElements(new IProject[] {project0}, context.getAllReferencingProjects());
		context = ContextBuilder.getBuilder(project2.getActiveVariant()).contextForLastBuild;
		assertEquals(0, context.getAllReferencedProjects().length);
		assertArraysContainSameElements(new IProject[] {project0, project1}, context.getAllReferencingProjects());
	}

	public void testWorkspaceBuildProjects() throws CoreException {
		setupSimpleReferences();
		ContextBuilder.clearStats();
		getWorkspace().build(new IProjectVariant[] {project0.getActiveVariant(), project2.getActiveVariant()}, IncrementalProjectBuilder.FULL_BUILD, getMonitor());
		IBuildContext context = ContextBuilder.getContext(project0.getActiveVariant());
		assertArraysContainSameElements(new IProject[] {project1}, context.getAllReferencedProjects());
		assertArraysContainSameElements(new IProject[] {}, context.getAllReferencingProjects());
		context = ContextBuilder.getBuilder(project1.getActiveVariant()).contextForLastBuild;
		assertArraysContainSameElements(new IProject[] {}, context.getAllReferencedProjects());
		assertArraysContainSameElements(new IProject[] {project0}, context.getAllReferencingProjects());
		context = ContextBuilder.getBuilder(project2.getActiveVariant()).contextForLastBuild;
		assertEquals(0, context.getAllReferencedProjects().length);
		assertEquals(0, context.getAllReferencingProjects().length);
	}

	public void testReferenceActiveVariant() throws CoreException {
		setReferences(project0.getActiveVariant(), new IProjectVariantReference[] {new ProjectVariantReference(project1)});
		setReferences(project1.getActiveVariant(), new IProjectVariantReference[] {new ProjectVariantReference(project2)});
		setReferences(project2.getActiveVariant(), new IProjectVariantReference[] {});

		ContextBuilder.clearStats();

		getWorkspace().build(new IProjectVariant[] {project0.getActiveVariant()}, IncrementalProjectBuilder.FULL_BUILD, getMonitor());

		IBuildContext context = ContextBuilder.getContext(project0.getActiveVariant());
		assertArraysContainSameElements(new IProject[] {project1, project2}, context.getAllReferencedProjects());
		assertEquals(0, context.getAllReferencingProjects().length);
		context = ContextBuilder.getBuilder(project1.getActiveVariant()).contextForLastBuild;
		assertArraysContainSameElements(new IProject[] {project2}, context.getAllReferencedProjects());
		assertArraysContainSameElements(new IProject[] {project0}, context.getAllReferencingProjects());
		context = ContextBuilder.getBuilder(project2.getActiveVariant()).contextForLastBuild;
		assertEquals(0, context.getAllReferencedProjects().length);
		assertArraysContainSameElements(new IProject[] {project0, project1}, context.getAllReferencingProjects());
	}

	/**
	 * Attempts to build a project that references the active variant of another project,
	 * and the same variant directly. This should only result in one referenced variant being built.
	 */
	public void testReferenceVariantTwice() throws CoreException {
		IProjectVariantReference ref1 = project1.newReference();
		IProjectVariantReference ref2 = project1.newReference();
		ref2.setVariantName(project1.getActiveVariant().getVariantName());
		setReferences(project0.getActiveVariant(), new IProjectVariantReference[] {ref1, ref2});
		setReferences(project1.getActiveVariant(), new IProjectVariantReference[] {});

		ContextBuilder.clearStats();

		getWorkspace().build(new IProjectVariant[] {project0.getActiveVariant()}, IncrementalProjectBuilder.FULL_BUILD, getMonitor());

		IBuildContext context = ContextBuilder.getContext(project0.getActiveVariant());
		assertArraysContainSameElements(new IProject[] {project1}, context.getAllReferencedProjects());
		assertEquals(0, context.getAllReferencingProjects().length);
		assertArraysContainSameElements(new IProjectVariant[] {project1.getActiveVariant()}, context.getAllReferencedProjectVariants());
		assertEquals(0, context.getAllReferencingProjectVariants().length);

		context = ContextBuilder.getBuilder(project1.getActiveVariant()).contextForLastBuild;
		assertEquals(0, context.getAllReferencedProjects().length);
		assertArraysContainSameElements(new IProject[] {project0}, context.getAllReferencingProjects());
		assertEquals(0, context.getAllReferencedProjectVariants().length);
		assertArraysContainSameElements(new IProjectVariant[] {project0.getActiveVariant()}, context.getAllReferencingProjectVariants());
	}

	private void setupSimpleReferences() throws CoreException {
		setReferences(project0.getActiveVariant(), new IProjectVariant[] {project1.getActiveVariant()});
		setReferences(project1.getActiveVariant(), new IProjectVariant[] {project2.getActiveVariant()});
		setReferences(project2.getActiveVariant(), new IProjectVariant[] {});
	}

	/**
	 * Helper method to set the references for a project.
	 */
	private void setReferences(IProjectVariant variant, IProjectVariantReference[] refs) throws CoreException {
		IProjectDescription desc = variant.getProject().getDescription();
		desc.setReferencedProjectVariants(variant.getVariantName(), refs);
		variant.getProject().setDescription(desc, getMonitor());
	}

	/**
	 * Helper method to set the references for a project.
	 */
	private void setReferences(IProjectVariant variant, IProjectVariant[] variants) throws CoreException {
		IProjectVariantReference[] refs = new IProjectVariantReference[variants.length];
		for (int i = 0; i < variants.length; i++)
			refs[i] = new ProjectVariantReference(variants[i]);
		setReferences(variant, refs);
	}

	private void assertArraysContainSameElements(IProjectVariant[] expected, IProjectVariant[] actual) {
		assertArraysContainSameElements(expected, actual, new Comparator() {
			public int compare(Object left, Object right) {
				IProjectVariant leftV = (IProjectVariant) left;
				IProjectVariant rightV = (IProjectVariant) right;
				int ret = leftV.getProject().getName().compareTo(rightV.getProject().getName());
				if (ret == 0)
					ret = leftV.getVariantName().compareTo(rightV.getVariantName());
				return ret;
			}
		});
	}

	private void assertArraysContainSameElements(IProject[] expected, IProject[] actual) {
		assertArraysContainSameElements(expected, actual, new Comparator() {
			public int compare(Object left, Object right) {
				return ((IProject) left).getName().compareTo(((IProject) right).getName());
			}
		});
	}

	/** Helper method to check if two project variant arrays contain the same elements, but in any order */
	private void assertArraysContainSameElements(Object[] expected, Object[] actual, Comparator comparator) {
		assertEquals(expected.length, actual.length);
		Arrays.sort(expected, comparator);
		Arrays.sort(actual, comparator);
		for (int i = 0; i < expected.length; i++)
			assertEquals(expected[i], actual[i]);
	}
}
