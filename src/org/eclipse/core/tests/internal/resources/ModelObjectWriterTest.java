package org.eclipse.core.tests.internal.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.*;
import java.util.HashMap;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.internal.localstore.SafeFileOutputStream;
import org.eclipse.core.internal.resources.*;
import org.eclipse.core.internal.utils.Policy;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.tests.harness.EclipseWorkspaceTest;
/**
 * 
 */
public class ModelObjectWriterTest extends EclipseWorkspaceTest {
public ModelObjectWriterTest() {
}
public ModelObjectWriterTest(String name) {
	super(name);
}
protected boolean contains(Object key, Object[] array) {
	for (int i = 0; i < array.length; i++)
		if (key.equals(array[i]))
			return true;
	return false;
}
protected String getInvalidWorkspaceDescription() {
	return 
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<workspaceDescription>\n" +
				"<name>Foo</name>\n" +
				"<autobuild>Foo</autobuild>\n" +
				"<snapshotInterval>300Foo000</snapshotInterval>\n" +
				"<fileStateLongevity>Foo480000</fileStateLongevity>\n" +
				"<maxFileStateSize>104856Foo</maxFileStateSize>\n" +
				"<maxFileStates>5Foo0</maxFileStates>\n" +
			"</workspaceDescription>\n";
}
public static Test suite() {
	return new TestSuite(ModelObjectWriterTest.class);
}
public void testProjectDescription() throws Throwable {

	/* initialize common objects */
	ModelObjectWriter writer = new ModelObjectWriter();
	ModelObjectReader reader = new ModelObjectReader();
	IPath root = getWorkspace().getRoot().getLocation();
	IPath location = root.append("ModelObjectWriterTest.pbs");
	/* test write */
	ProjectDescription description = new ProjectDescription();
	description.setLocation(location);
	description.setName("MyProjectDescription");
	HashMap args = new HashMap(3);
	args.put("ArgOne", "ARGH!");
	args.put("ArgTwo", "2 x ARGH!");
	args.put("NullArg", null);
	args.put("EmptyArg", "");
	ICommand[] commands = new ICommand[2];
	commands[0] = description.newCommand();
	commands[0].setBuilderName("MyCommand");
	commands[0].setArguments(args);
	commands[1] = description.newCommand();
	commands[1].setBuilderName("MyOtherCommand");
	commands[1].setArguments(args);
	description.setBuildSpec(commands);

	SafeFileOutputStream output = new SafeFileOutputStream(location.toFile());
	writer.write(description, output);
	output.close();

	/* test read */
	FileInputStream input = new FileInputStream(location.toFile());
	ProjectDescription description2 = (ProjectDescription) reader.read(input);
	assertTrue("1.1", description.getName().equals(description2.getName()));
	assertTrue("1.3", location.equals(description.getLocation()));

	ICommand[] commands2 = description2.getBuildSpec();
	assertEquals("2.00", 2, commands2.length);
	assertEquals("2.01", "MyCommand", commands2[0].getBuilderName());
	assertEquals("2.02", "ARGH!", commands2[0].getArguments().get("ArgOne"));
	assertEquals("2.03", "2 x ARGH!", commands2[0].getArguments().get("ArgTwo"));
	assertEquals("2.04", "", commands2[0].getArguments().get("NullArg"));
	assertEquals("2.05", "", commands2[0].getArguments().get("EmptyArg"));
	assertEquals("2.06", "MyOtherCommand", commands2[1].getBuilderName());
	assertEquals("2.07", "ARGH!", commands2[1].getArguments().get("ArgOne"));
	assertEquals("2.08", "2 x ARGH!", commands2[1].getArguments().get("ArgTwo"));
	assertEquals("2.09", "", commands2[0].getArguments().get("NullArg"));
	assertEquals("2.10", "", commands2[0].getArguments().get("EmptyArg"));

	/* remove trash */
	Workspace.clear(location.toFile());
}
public void testInvalidWorkspaceDescription() {
	/* initialize common objects */
	ModelObjectReader reader = new ModelObjectReader();
	IPath root = getWorkspace().getRoot().getLocation();
	IPath location = root.append("ModelObjectWriterTest2.pbs");

	/* write the bogus description */
	try {
		FileWriter writer = new FileWriter(location.toFile());
		writer.write(getInvalidWorkspaceDescription());
		writer.close();
	} catch (IOException e) {
		fail("1.91", e);
	}

	/* test read */
	try {
		FileInputStream input = null;
		try {
			input = new FileInputStream(location.toFile());
		} catch (FileNotFoundException e) {
			fail("1.99", e);
		}
		//on reading invalid values the reader should revert to default values
		WorkspaceDescription desc2 = (WorkspaceDescription) reader.read(input);
		//assertion "1.1" removed because workspace name can't be invalid
		assertTrue("1.2", Policy.defaultAutoBuild == desc2.isAutoBuilding());
		assertTrue("1.3", Policy.defaultDeltaExpiration == desc2.getDeltaExpiration());
		assertTrue("1.4", Policy.defaultFileStateLongevity == desc2.getFileStateLongevity());
		assertTrue("1.5", Policy.defaultMaxFileStates == desc2.getMaxFileStates());
		assertTrue("1.6", Policy.defaultMaxFileStateSize == desc2.getMaxFileStateSize());
		assertTrue("1.7", Policy.defaultOperationsPerSnapshot == desc2.getOperationsPerSnapshot());
		assertTrue("1.8", Policy.defaultSnapshots == desc2.isSnapshotEnabled());
	} finally {
		/* remove trash */
		Workspace.clear(location.toFile());
	}
}public void testWorkspaceDescription() throws Throwable {

	/* initialize common objects */
	ModelObjectWriter writer = new ModelObjectWriter();
	ModelObjectReader reader = new ModelObjectReader();
	IPath root = getWorkspace().getRoot().getLocation();
	IPath location = root.append("ModelObjectWriterTest.pbs");

	/* test write */
	WorkspaceDescription desc = new WorkspaceDescription("MyWorkspace");
	desc.setName("aName");
	desc.setAutoBuilding(false);
	desc.setFileStateLongevity(654321l);
	desc.setMaxFileStates(1000);
	desc.setMaxFileStateSize(123456789l);

	SafeFileOutputStream output = new SafeFileOutputStream(location.toFile());
	writer.write(desc, output);
	output.close();

	/* test read */
	try {
		FileInputStream input = new FileInputStream(location.toFile());
		WorkspaceDescription desc2 = (WorkspaceDescription) reader.read(input);
		assertTrue("1.1", desc.getName().equals(desc2.getName()));
		assertTrue("1.2", desc.isAutoBuilding() == desc2.isAutoBuilding());
		assertTrue("1.3", desc.getDeltaExpiration() == desc2.getDeltaExpiration());
		assertTrue("1.4", desc.getFileStateLongevity() == desc2.getFileStateLongevity());
		assertTrue("1.5", desc.getMaxFileStates() == desc2.getMaxFileStates());
		assertTrue("1.6", desc.getMaxFileStateSize() == desc2.getMaxFileStateSize());
		assertTrue("1.7", desc.getOperationsPerSnapshot() == desc2.getOperationsPerSnapshot());
		assertTrue("1.8", desc.isSnapshotEnabled() == desc2.isSnapshotEnabled());
	} finally {
		/* remove trash */
		Workspace.clear(location.toFile());
	}
}
}
