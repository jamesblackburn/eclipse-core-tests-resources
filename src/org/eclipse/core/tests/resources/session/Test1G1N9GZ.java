package org.eclipse.core.tests.resources.session;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.tests.internal.builders.SortBuilder;

/**
 * Regression test for 1G1N9GZ: ITPCORE:WIN2000 - ElementTree corruption when linking trees
 */
public class Test1G1N9GZ extends WorkspaceSerializationTest {
/**
 * Constructor for Test1G1N9GZ.
 */
public Test1G1N9GZ() {
	super();
}
/**
 * Constructor for Test1G1N9GZ.
 * @param name
 */
public Test1G1N9GZ(String name) {
	super(name);
}
/**
 * Initial setup and save
 */
public void test1() throws CoreException {
	/* create P1 and set a builder */
	IProject p1 = workspace.getRoot().getProject("p1");
	p1.create(null);
	p1.open(null);
	IProjectDescription desc = p1.getDescription();
	ICommand command = desc.newCommand();
	command.setBuilderName(SortBuilder.BUILDER_NAME);
	command.getArguments().put(SortBuilder.BUILD_ID, "P1Build1");
	desc.setBuildSpec(new ICommand[] {command});
	setUpMonitor();
	p1.setDescription(desc, monitor);

	/* create P2 and set a builder */
	IProject p2 = workspace.getRoot().getProject("p2");
	p2.create(null);
	p2.open(null);
	desc = p1.getDescription();
	command = desc.newCommand();
	command.setBuilderName(SortBuilder.BUILDER_NAME);
	command.getArguments().put(SortBuilder.BUILD_ID, "P2Build1");
	desc.setBuildSpec(new ICommand[] {command});
	setUpMonitor();
	p1.setDescription(desc, monitor);

	/* PR test case */
	setUpMonitor();
	workspace.save(true, monitor);
}
public void test2() throws CoreException {
	setUpMonitor();
	workspace.save(true, monitor);
}
public void test3() throws CoreException {	
	/* open again and try to create a files */
	ResourcesPlugin.getPlugin().startup();

	/* get new handles */
	IProject p1 = workspace.getRoot().getProject("p1");
	IProject p2 = workspace.getRoot().getProject("p2");

	/* try to create other files */
	try {
		ByteArrayInputStream source = new ByteArrayInputStream("file's content".getBytes());
		p1.getFile("file2").create(source, true, null);
	} catch (Exception e) {
		fail("1.0", e);
	}
	try {
		ByteArrayInputStream source = new ByteArrayInputStream("file's content".getBytes());
		p2.getFile("file2").create(source, true, null);
	} catch (Exception e) {
		fail("1.1", e);
	}
}
}