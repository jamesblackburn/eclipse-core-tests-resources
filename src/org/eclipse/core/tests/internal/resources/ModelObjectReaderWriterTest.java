/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.internal.resources;


import java.io.*;
import java.net.URL;
import java.util.*;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.internal.localstore.SafeFileOutputStream;
import org.eclipse.core.internal.plugins.PluginDescriptor;
import org.eclipse.core.internal.resources.*;
import org.eclipse.core.internal.utils.Policy;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.tests.harness.EclipseWorkspaceTest;
import org.xml.sax.InputSource;
/**
 * 
 */
public class ModelObjectReaderWriterTest extends EclipseWorkspaceTest {
	static final IPath LONG_LOCATION = new Path("D:/eclipse/dev/i0218/eclipse/pffds/fds//fds///fdsfsdfsd///fdsfdsf/fsdfsdfsd/lugi/dsds/fsd//f/ffdsfdsf/fsdfdsfsd/fds//fdsfdsfdsf/fdsfdsfds/fdsfdsfdsf/fdsfdsfdsds/ns/org.eclipse.help.ui_2.1.0/contexts.xml");
public ModelObjectReaderWriterTest() {
}
public ModelObjectReaderWriterTest(String name) {
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
//	TestSuite suite = new TestSuite();
//	suite.addTest(new ModelObjectReaderWriterTest("testMultipleProjectDescriptions"));
//	return suite;
	return new TestSuite(ModelObjectReaderWriterTest.class);
}
public void testProjectDescription() throws Throwable {
	// Use ModelObject2 to read the project description

	/* initialize common objects */
	ModelObjectWriter writer = new ModelObjectWriter();
	ProjectDescriptionReader reader = new ProjectDescriptionReader();
	IPath root = getWorkspace().getRoot().getLocation();
	IPath location = root.append("ModelObjectWriterTest2.pbs");
	String defaultCharset = "ISO-1234-5";
	/* test write */
	ProjectDescription description = new ProjectDescription();
	description.setLocation(location);
	description.setName("MyProjectDescription");
	description.setDefaultCharset(defaultCharset);
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
	InputSource in = new InputSource(input);
	ProjectDescription description2 = reader.read(in);
	assertTrue("1.1", description.getName().equals(description2.getName()));
	assertEquals("1.2", description.getDefaultCharset(), description2.getDefaultCharset());
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
public void testProjectDescription2() throws Throwable {
	// Use ModelObject2 to read the project description

	/* initialize common objects */
	ModelObjectWriter writer = new ModelObjectWriter();
	ProjectDescriptionReader reader = new ProjectDescriptionReader();
	IPath root = getWorkspace().getRoot().getLocation();
	IPath location = root.append("ModelObjectWriterTest3.pbs");
	/* test write */
	ProjectDescription description = new ProjectDescription();
	description.setLocation(location);
	description.setName("MyProjectDescription");
	HashMap args = new HashMap(3);
	args.put("ArgOne", "ARGH!");
	ICommand[] commands = new ICommand[1];
	commands[0] = description.newCommand();
	commands[0].setBuilderName("MyCommand");
	commands[0].setArguments(args);
	description.setBuildSpec(commands);
	String comment = "Now is the time for all good men to come to the aid of the party.  Now is the time for all good men to come to the aid of the party.  Now is the time for all good men to come to the aid of the party.";
	description.setComment(comment);
	IProject[] refProjects = new IProject[3];
	refProjects[0] = ResourcesPlugin.getWorkspace().getRoot().getProject("org.eclipse.core.runtime");
	refProjects[1] = ResourcesPlugin.getWorkspace().getRoot().getProject("org.eclipse.core.boot");
	refProjects[2] = ResourcesPlugin.getWorkspace().getRoot().getProject("org.eclipse.core.resources");
	description.setReferencedProjects(refProjects);

	SafeFileOutputStream output = new SafeFileOutputStream(location.toFile());
	writer.write(description, output);
	output.close();

	/* test read */
	FileInputStream input = new FileInputStream(location.toFile());
	InputSource in = new InputSource(input);
	ProjectDescription description2 = reader.read(in);
	assertTrue("1.1", description.getName().equals(description2.getName()));
	assertEquals("1.2", description.getDefaultCharset(), description2.getDefaultCharset());
	assertTrue("1.3", location.equals(description.getLocation()));

	ICommand[] commands2 = description2.getBuildSpec();
	assertEquals("2.00", 1, commands2.length);
	assertEquals("2.01", "MyCommand", commands2[0].getBuilderName());
	assertEquals("2.02", "ARGH!", commands2[0].getArguments().get("ArgOne"));
	
	assertTrue("3.0", description.getComment().equals(description2.getComment()));
	
	IProject[] ref = description.getReferencedProjects();
	IProject[] ref2 = description2.getReferencedProjects();
	assertEquals("4.0", 3, ref2.length);
	assertTrue("4.1", ref[0].getName().equals(ref2[0].getName()));
	assertTrue("4.2", ref[1].getName().equals(ref2[1].getName()));
	assertTrue("4.3", ref[2].getName().equals(ref2[2].getName()));

	/* remove trash */
	Workspace.clear(location.toFile());
}
public void testInvalidWorkspaceDescription() {
	/* initialize common objects */
	WorkspaceDescriptionReader reader = new WorkspaceDescriptionReader();
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
}
private String[] getPathMembers(URL path) {
	String[] list = null;
	String protocol = path.getProtocol();
	if (protocol.equals("file")) { //$NON-NLS-1$
		list = (new java.io.File(path.getFile())).list();
	} else {
		// XXX: attempt to read URL and see if we got html dir page
	}
	return list == null ? new String[0] : list;
}
private void compareProjectDescriptions (int errorTag, ProjectDescription description, ProjectDescription description2) {
	assertTrue (errorTag + ".0", description.getName().equals(description2.getName()));
	String comment = description.getComment();
	if (comment == null) {
		// The old reader previously returned null for an empty comment.  We
		// are changing this so it now returns an empty string.
		assertEquals (errorTag + ".1", 0, description2.getComment().length());
	} else
		assertTrue (errorTag + ".2", description.getComment().equals(description2.getComment()));

	IProject[] projects = description.getReferencedProjects();
	IProject[] projects2 = description2.getReferencedProjects();
	compareProjects(errorTag, projects, projects2);

	ICommand[] commands = description.getBuildSpec();
	ICommand[] commands2 = description2.getBuildSpec();
	compareBuildSpecs(errorTag, commands, commands2);

	String[] natures = description.getNatureIds();
	String[] natures2 = description2.getNatureIds();
	compareNatures(errorTag, natures, natures2);

	HashMap links = description.getLinks();
	HashMap links2 = description2.getLinks();
	compareLinks(errorTag, links, links2);
}
private void compareProjects (int errorTag, IProject[] projects, IProject[] projects2) {
	// ASSUMPTION:  projects and projects2 are non-null
	assertEquals (errorTag + ".1.0", projects.length, projects2.length);
	for (int i = 0; i < projects.length; i++) {
		assertTrue(errorTag + ".1." + (i + 1), projects[i].getName().equals(projects2[i].getName()));
	}
}
private void compareBuildSpecs (int errorTag, ICommand[] commands, ICommand[] commands2) {
	// ASSUMPTION:  commands and commands2 are non-null
	assertEquals (errorTag + ".2.0", commands.length, commands2.length);
	for (int i = 0; i < commands.length; i++) {
		assertTrue(errorTag + ".2." + (i + 1) + "0", commands[i].getBuilderName().equals(commands2[i].getBuilderName()));
		Map args = commands[i].getArguments();
		Map args2 = commands2[i].getArguments();
		assertEquals (errorTag + ".2." + (i + 1) + "0", args.size(), args2.size());
		Set keys = args.keySet();
		int x = 1;
		for (Iterator j = keys.iterator(); j.hasNext(); x++) {
			Object key = j.next();
			String value = (String)args.get(key);
			String value2 = (String)args2.get(key);
			if (value == null)
				assertNull(errorTag + ".2."  + (i + 1) + x, value2);
			else
				assertTrue(errorTag + ".3."  + (i + 1) + x, ((String)args.get(key)).equals(((String)args2.get(key))));
		}
	}
}
private void compareNatures (int errorTag, String[] natures, String[] natures2) {
	// ASSUMPTION:  natures and natures2 are non-null
	assertEquals (errorTag + ".3.0", natures.length, natures2.length);
	for (int i = 0; i < natures.length; i++) {
		assertTrue(errorTag + ".3." + (i + 1), natures[i].equals(natures2[i]));
	}
}

public void testInvalidProjectDescription4() throws Throwable {
	String invalidProjectDescription = 
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
		"<projectDescription>\n" +
		"	<name>abc</name>\n" +
		"	<comment></comment>\n" +
		"	<projects>\n" +
		"	</projects>\n" +
		"	<buildSpec>\n" +
		"	</buildSpec>\n" +
		"	<natures>\n" +
		"	</natures>\n" +
		"	<linkedResources>\n" +
		"		<link>\n" +
		"			<name>newLink</name>\n" +
		"			<type>foobar</type>\n" +
		"			<location>d:/abc/def</location>\n" +
		"		</link>\n" +
		"	</linkedResources>\n" +
		"</projectDescription>";

	IPath root = getWorkspace().getRoot().getLocation();
	IPath location = root.append("ModelObjectReaderWriterTest.txt");
	ProjectDescriptionReader reader = new ProjectDescriptionReader();
	// Write out the project description file
	ensureDoesNotExistInFileSystem(location.toFile());
	InputStream stream = new ByteArrayInputStream(invalidProjectDescription.getBytes());
	createFileInFileSystem(location, stream);
	try {
		ProjectDescription projDesc = reader.read(location);
		
		assertNotNull ("3.0", projDesc);
		assertTrue("3.1", projDesc.getName().equals("abc"));
		assertEquals("3.2", 0, projDesc.getComment().length());
		assertNull("3.3", projDesc.getLocation());
		assertEquals("3.4", new IProject[0], projDesc.getReferencedProjects());
		assertEquals("3.5", new String[0], projDesc.getNatureIds());
		assertEquals("3.6", new ICommand[0], projDesc.getBuildSpec());
		LinkDescription link = (LinkDescription)projDesc.getLinks().values().iterator().next();
		assertEquals("3.7", "newLink", link.getName());
		assertEquals("3.8", "d:/abc/def", link.getLocation().toString());
	} finally {
		Workspace.clear(location.toFile());
	}
}
private void compareLinks (int errorTag, HashMap links, HashMap links2) {
	if (links == null) {
		assertNull (errorTag + ".4.0", links2);
		return;
	}
	assertEquals (errorTag + ".4.01", links.size(), links2.size());
	Set keys = links.keySet();
	int x = 1;
	for (Iterator i = keys.iterator(); i.hasNext(); x++) {
		String key = (String)i.next();
		LinkDescription value = (LinkDescription)links.get(key);
		LinkDescription value2 = (LinkDescription)links2.get(key);
		assertTrue(errorTag + ".4." + x, value.getName().equals(value2.getName()));
		assertEquals(errorTag + ".5." + x, value.getType(), value2.getType());
		assertTrue(errorTag + ".6." + x, value.getLocation().equals(value2.getLocation()));
	}
}
public void testInvalidProjectDescription1() throws Throwable {
	String invalidProjectDescription = 
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
		"<homeDescription>\n" +
		"	<name>abc</name>\n" +
		"	<comment></comment>\n" +
		"	<projects>\n" +
		"	</projects>\n" +
		"	<buildSpec>\n" +
		"		<buildCommand>\n" +
		"			<name>org.eclipse.jdt.core.javabuilder</name>\n" +
		"			<arguments>\n" +
		"			</arguments>\n" +
		"		</buildCommand>\n" +
		"	</buildSpec>\n" +
		"	<natures>\n" +
		"	<nature>org.eclipse.jdt.core.javanature</nature>\n" +
		"	</natures>\n" +
		"	<linkedResources>\n" +
		"		<link>\n" +
		"			<name>newLink</name>\n" +
		"			<type>2</type>\n" +
		"			<location>d:/abc/def</location>\n" +
		"		</link>\n" +
		"	</linkedResources>\n" +
		"</homeDescription>";

	IPath root = getWorkspace().getRoot().getLocation();
	IPath location = root.append("ModelObjectReaderWriterTest.txt");
	ProjectDescriptionReader reader = new ProjectDescriptionReader();
	// Write out the project description file
	ensureDoesNotExistInFileSystem(location.toFile());
	InputStream stream = new ByteArrayInputStream(invalidProjectDescription.getBytes());
	createFileInFileSystem(location, stream);
	try {
		ProjectDescription projDesc = reader.read(location);
	
		assertNull ("1.0", projDesc);		
	} finally {
		Workspace.clear(location.toFile());
	}
}
public void testInvalidProjectDescription2() throws Throwable {
	String invalidProjectDescription = 
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
		"<projectDescription>\n" +
		"	<bogusname>abc</bogusname>\n" +
		"</projectDescription>";

	IPath root = getWorkspace().getRoot().getLocation();
	IPath location = root.append("ModelObjectReaderWriterTest.txt");
	ProjectDescriptionReader reader = new ProjectDescriptionReader();
	// Write out the project description file
	ensureDoesNotExistInFileSystem(location.toFile());
	InputStream stream = new ByteArrayInputStream(invalidProjectDescription.getBytes());
	createFileInFileSystem(location, stream);
	try {
		ProjectDescription projDesc = reader.read(location);
		
		assertNotNull ("2.0", projDesc);
		assertNull("2.1", projDesc.getName());
		assertEquals("2.2", 0, projDesc.getComment().length());
		assertNull("2.3", projDesc.getLocation());
		assertEquals("2.4", new IProject[0], projDesc.getReferencedProjects());
		assertEquals("2.5", new String[0], projDesc.getNatureIds());
		assertEquals("2.6", new ICommand[0], projDesc.getBuildSpec());
		assertNull("2.7", projDesc.getLinks());
	} finally {
		Workspace.clear(location.toFile());
	}
}
public void testInvalidProjectDescription3() throws Throwable {
	String invalidProjectDescription = 
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
		"<projectDescription>\n" +
		"	<name>abc</name>\n" +
		"	<comment></comment>\n" +
		"	<projects>\n" +
		"	</projects>\n" +
		"	<buildSpec>\n" +
		"		<badBuildCommand>\n" +
		"			<name>org.eclipse.jdt.core.javabuilder</name>\n" +
		"			<arguments>\n" +
		"			</arguments>\n" +
		"		</badBuildCommand>\n" +
		"	</buildSpec>\n" +
		"</projectDescription>";

	IPath root = getWorkspace().getRoot().getLocation();
	IPath location = root.append("ModelObjectReaderWriterTest.txt");
	ProjectDescriptionReader reader = new ProjectDescriptionReader();
	// Write out the project description file
	ensureDoesNotExistInFileSystem(location.toFile());
	InputStream stream = new ByteArrayInputStream(invalidProjectDescription.getBytes());
	createFileInFileSystem(location, stream);
	try {
		ProjectDescription projDesc = reader.read(location);
		
		assertNotNull ("3.0", projDesc);
		assertTrue("3.1", projDesc.getName().equals("abc"));
		assertEquals("3.2", 0, projDesc.getComment().length());
		assertNull("3.3", projDesc.getLocation());
		assertEquals("3.4", new IProject[0], projDesc.getReferencedProjects());
		assertEquals("3.5", new String[0], projDesc.getNatureIds());
		assertEquals("3.6", new ICommand[0], projDesc.getBuildSpec());
		assertNull("3.7", projDesc.getLinks());
	} finally {
		Workspace.clear(location.toFile());
	}
}
private String getLongDescription() {
	return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + 
	"<projectDescription>" +
		"<name>org.eclipse.help.ui</name>" +
		"<comment></comment>" +
		"<charset>UTF-8</charset>" +
	"	<projects>" +
		"	<project>org.eclipse.core.boot</project>" +
		"	<project>org.eclipse.core.resources</project>" +
		"	<project>org.eclipse.core.runtime</project>" +
		"	<project>org.eclipse.help</project>" +
		"	<project>org.eclipse.help.appserver</project>" +
		"	<project>org.eclipse.search</project>" +
		"	<project>org.eclipse.ui</project>" +
	"	</projects>" +
	"	<buildSpec>" +
		"	<buildCommand>" +
			"	<name>org.eclipse.jdt.core.javabuilder</name>" +
			"	<arguments>" +
			"	</arguments>" +
		"	</buildCommand>" +
		"	<buildCommand>" +
			"	<name>org.eclipse.pde.ManifestBuilder</name>" +
			"	<arguments>" +
			"	</arguments>" +
		"	</buildCommand>" +
		"	<buildCommand>" +
			"	<name>org.eclipse.pde.SchemaBuilder</name>" +
			"	<arguments>" +
			"	</arguments>" +
		"	</buildCommand>" +
	"	</buildSpec>" +
	"	<natures>" +
		"	<nature>org.eclipse.jdt.core.javanature</nature>" +
		"	<nature>org.eclipse.pde.PluginNature</nature>" +
	"	</natures>" +
	"	<linkedResources>" +
		"	<link>" +
			"	<name>contexts.xml</name>" +
			"	<type>1</type>" +
			"	<location>" + LONG_LOCATION + "</location>" +
		"	</link>" +
		"	<link>" +
			"	<name>doc</name>" +
			"	<type>2</type>" +
			"	<location>" + LONG_LOCATION + "</location>" +
		"	</link>" +
		"	<link>" +
			"	<name>icons</name>" +
			"	<type>2</type>" +
			"	<location>" + LONG_LOCATION + "</location>" +
		"	</link>" +
		"	<link>" +
			"	<name>preferences.ini</name>" +
			"	<type>1</type>" +
			"	<location>" + LONG_LOCATION + "</location>" +
		"	</link>" +
		"	<link>" +
			"	<name>.options</name>" +
			"	<type>1</type>" +
			"	<location>" + LONG_LOCATION + "</location>" +
		"	</link>" +
		"	<link>" +
			"	<name>plugin.properties</name>" +
			"	<type>1</type>" +
			"	<location>" + LONG_LOCATION + "</location>" +
		"	</link>" +
		"	<link>" +
			"	<name>plugin.xml</name>" +
			"	<type>1</type>" +
			"	<location>" + LONG_LOCATION + "</location>" +
		"	</link>" +
		"	<link>" +
			"	<name>about.html</name>" +
			"	<type>1</type>" +
			"	<location>" + LONG_LOCATION + "</location>" +
		"	</link>" +
		"	<link>" +
			"	<name>helpworkbench.jar</name>" +
			"	<type>1</type>" +
			"	<location>" + LONG_LOCATION + "</location>" +
		"	</link>" +
		"</linkedResources>" +
	"</projectDescription>";
}
/**
 * Test multiple elements where they shouldn't be
 */
public void testLongProjectDescription() throws Throwable {
	String longProjectDescription = getLongDescription();

	IPath location = getRandomLocation();
	try {
		ProjectDescriptionReader reader = new ProjectDescriptionReader();
		// Write out the project description file
		ensureDoesNotExistInFileSystem(location.toFile());
		InputStream stream = new ByteArrayInputStream(longProjectDescription.getBytes());
		createFileInFileSystem(location, stream);
		ProjectDescription projDesc = reader.read(location);
		ensureDoesNotExistInFileSystem(location.toFile());
		for (Iterator i = projDesc.getLinks().values().iterator(); i.hasNext(); ) {
			LinkDescription link = (LinkDescription) i.next();
			assertEquals("1.0." + link.getName(), LONG_LOCATION, link.getLocation());
		}
	} finally {
		Workspace.clear(location.toFile());
	}
}
public void testMultipleProjectDescriptions() throws Throwable {
	URL whereToLook = null;
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.resources");
	String pluginPath = Platform.resolve(tempPlugin.getInstallURL()).toExternalForm().concat("MultipleProjectTestFiles/");
	try {
		whereToLook = new URL (pluginPath);
	} catch (java.net.MalformedURLException e) {
		assertTrue("Bad URL for " + pluginPath, true);
	}
	String[] members = {"abc.project","def.project",
		"org.apache.lucene.project", "org.eclipse.ant.core.project"};
	HashMap baselines = buildBaselineDescriptors(new Path(pluginPath));
	ProjectDescriptionReader reader = new ProjectDescriptionReader();
	
	for (int i = 0; i < members.length; i++) {
		URL currentURL = null;
		currentURL = new URL(whereToLook, members[i]);
		InputStream is = null;
		try {
			is = currentURL.openStream();
		} catch (IOException e) {
			fail("0.5");
		}
		InputSource in = new InputSource(is);
		ProjectDescription description = reader.read(in);
		
		compareProjectDescriptions(i + 1, description, (ProjectDescription)baselines.get(members[i]));
	}
}
private HashMap buildBaselineDescriptors(IPath pluginPath) {
	HashMap result = new HashMap();
	IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

	ProjectDescription desc = new ProjectDescription();
	desc.setName("abc.project");
	ICommand[] commands = new ICommand[1];
	commands[0] = desc.newCommand();
	commands[0].setBuilderName("org.eclipse.jdt.core.javabuilder");
	desc.setBuildSpec(commands);
	String[] natures = new String[1];
	natures[0] = "org.eclipse.jdt.core.javanature";
	desc.setNatureIds(natures);
	HashMap linkMap = new HashMap();
	LinkDescription link = new LinkDescription("newLink", 2, new Path("d:/abc/def"));
	linkMap.put(link.getName(), link);
	desc.setLinkDescriptions(linkMap);
	result.put(desc.getName(), desc);
	commands = null;
	natures = null;
	link = null;
	linkMap = null;
	desc = null;

	desc = new ProjectDescription();
	desc.setName("def.project");
	commands = new ICommand[1];
	commands[0] = desc.newCommand();
	commands[0].setBuilderName("org.eclipse.jdt.core.javabuilder");
	desc.setBuildSpec(commands);
	natures = new String[1];
	natures[0] = "org.eclipse.jdt.core.javanature";
	desc.setNatureIds(natures);
	linkMap = new HashMap();
	link = new LinkDescription("newLink", 2, new Path("d:/abc/def"));
	linkMap.put(link.getName(), link);
	link = new LinkDescription("link2", 2, new Path("d:/abc"));
	linkMap.put(link.getName(), link);
	link = new LinkDescription("link3", 2, new Path("d:/abc/def/ghi"));
	linkMap.put(link.getName(), link);
	link = new LinkDescription("link4", 1, new Path("d:/abc/def/afile.txt"));
	linkMap.put(link.getName(), link);
	desc.setLinkDescriptions(linkMap);
	result.put(desc.getName(), desc);
	commands = null;
	natures = null;
	link = null;
	linkMap = null;
	desc = null;

	desc = new ProjectDescription();
	desc.setName("org.apache.lucene.project");
	IProject[] refProjects = new Project[2];
	refProjects[0] = root.getProject("org.eclipse.core.boot");
	refProjects[1] = root.getProject("org.eclipse.core.runtime");
	desc.setReferencedProjects(refProjects);
	commands = new ICommand[3];
	commands[0] = desc.newCommand();
	commands[0].setBuilderName("org.eclipse.jdt.core.javabuilder");
	commands[1] = desc.newCommand();
	commands[1].setBuilderName("org.eclipse.pde.ManifestBuilder");
	commands[2] = desc.newCommand();
	commands[2].setBuilderName("org.eclipse.pde.SchemaBuilder");
	desc.setBuildSpec(commands);
	natures = new String[2];
	natures[0] = "org.eclipse.jdt.core.javanature";
	natures[1] = "org.eclipse.pde.PluginNature";
	desc.setNatureIds(natures);
	result.put(desc.getName(), desc);
	refProjects = null;
	commands = null;
	natures = null;
	desc = null;

	desc = new ProjectDescription();
	desc.setName("org.eclipse.ant.core.project");
	refProjects = new Project[4];
	refProjects[0] = root.getProject("org.apache.ant");
	refProjects[1] = root.getProject("org.apache.xerces");
	refProjects[2] = root.getProject("org.eclipse.core.boot");
	refProjects[3] = root.getProject("org.eclipse.core.runtime");
	desc.setReferencedProjects(refProjects);
	commands = new ICommand[2];
	commands[0] = desc.newCommand();
	commands[0].setBuilderName("org.eclipse.jdt.core.javabuilder");
	commands[1] = desc.newCommand();
	commands[1].setBuilderName("org.eclipse.ui.externaltools.ExternalToolBuilder");
	Map argMap = new HashMap();
	argMap.put("!{tool_show_log}", "true");
	argMap.put("!{tool_refresh}", "${none}");
	argMap.put("!{tool_name}", "org.eclipse.ant.core extra builder");
	argMap.put("!{tool_dir}", "");
	argMap.put("!{tool_args}", "-DbuildType=${build_type}");
	argMap.put("!{tool_loc}", "${workspace_loc:/org.eclipse.ant.core/scripts/buildExtraJAR.xml}");
	argMap.put("!{tool_type}", "org.eclipse.ui.externaltools.type.ant");
	commands[1].setArguments(argMap);
	desc.setBuildSpec(commands);
	natures = new String[1];
	natures[0] = "org.eclipse.jdt.core.javanature";
	desc.setNatureIds(natures);
	result.put(desc.getName(), desc);
	refProjects = null;
	commands = null;
	natures = null;
	desc = null;
	
	return result;
}
public void testWorkspaceDescription() throws Throwable {

	/* initialize common objects */
	ModelObjectWriter writer = new ModelObjectWriter();
	WorkspaceDescriptionReader reader = new WorkspaceDescriptionReader();
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
public void testMultiLineCharFields() throws Throwable {
	String multiLineProjectDescription = 
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
		"<projectDescription>\n" +
		"	<name>\n" +
		"      abc\n" +
		"   </name>\n" +
		"	<charset>\n" + 
		"		ISO-8859-1\n" +
		"	</charset>\n" +
		"	<comment>This is the comment.</comment>\n" +
		"	<projects>\n" +
		"	   <project>\n" +
		"         org.eclipse.core.boot\n" +
		"      </project>\n" +
		"	</projects>\n" +
		"	<buildSpec>\n" +
		"		<buildCommand>\n" +
		"			<name>\n" +
		"              org.eclipse.jdt.core.javabuilder\n" +
		"           </name>\n" +
		"			<arguments>\n" +
		"              <key>thisIsTheKey</key>\n" +
		"              <value>thisIsTheValue</value>\n" +
		"			</arguments>\n" +
		"		</buildCommand>\n" +
		"	</buildSpec>\n" +
		"	<natures>\n" +
		"	   <nature>\n" +
		"         org.eclipse.jdt.core.javanature\n" +
		"      </nature>\n" +
		"	</natures>\n" +
		"	<linkedResources>\n" +
		"		<link>\n" +
		"			<name>\n" +
		"              newLink\n" +
		"           </name>\n" +
		"			<type>\n" +
		"              2\n" +
		"           </type>\n" +
		"			<location>\n" +
		"              d:/abc/def\n" +
		"           </location>\n" +
		"		</link>\n" +
		"	</linkedResources>\n" +
		"</projectDescription>";

	String singleLineProjectDescription = 
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
		"<projectDescription>\n" +
		"	<name>abc</name>\n" +
		"	<charset>ISO-8859-1</charset>\n" +		
		"	<comment>This is the comment.</comment>\n" +
		"	<projects>\n" +
		"	   <project>org.eclipse.core.boot</project>\n" +
		"	</projects>\n" +
		"	<buildSpec>\n" +
		"		<buildCommand>\n" +
		"			<name>org.eclipse.jdt.core.javabuilder</name>\n" +
		"			<arguments>\n" +
		"              <key>thisIsTheKey</key>\n" +
		"              <value>thisIsTheValue</value>\n" +
		"			</arguments>\n" +
		"		</buildCommand>\n" +
		"	</buildSpec>\n" +
		"	<natures>\n" +
		"	   <nature>org.eclipse.jdt.core.javanature</nature>\n" +
		"	</natures>\n" +
		"	<linkedResources>\n" +
		"		<link>\n" +
		"			<name>newLink</name>\n" +
		"			<type>2</type>\n" +
		"			<location>d:/abc/def</location>\n" +
		"		</link>\n" +
		"	</linkedResources>\n" +
		"</projectDescription>";

	IPath root = getWorkspace().getRoot().getLocation();
	IPath multiLocation = root.append("multiLineTest.txt");
	IPath singleLocation = root.append("singleLineTest.txt");
	ProjectDescriptionReader reader = new ProjectDescriptionReader();
	// Write out the project description file
	ensureDoesNotExistInFileSystem(multiLocation.toFile());
	ensureDoesNotExistInFileSystem(singleLocation.toFile());
	InputStream multiStream = new ByteArrayInputStream(multiLineProjectDescription.getBytes());
	InputStream singleStream = new ByteArrayInputStream(singleLineProjectDescription.getBytes());
	try {
		createFileInFileSystem(multiLocation, multiStream);
		createFileInFileSystem(singleLocation, singleStream);
		ProjectDescription multiDesc = reader.read(multiLocation);
		ProjectDescription singleDesc = reader.read(singleLocation);
		compareProjectDescriptions(1, multiDesc, singleDesc);
	} finally {
		Workspace.clear(multiLocation.toFile());
		Workspace.clear(singleLocation.toFile());
	}
}
}
