package org.eclipse.core.tests.resources;

import org.eclipse.core.boot.BootLoader;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.tests.harness.EclipseWorkspaceTest;
import junit.framework.*;
import junit.textui.TestRunner;

public class IFolderTest extends EclipseWorkspaceTest {
public IFolderTest() {
}
public IFolderTest(String name) {
	super(name);
}
protected void setUp() throws Exception {
	super.setUp();
}
public static Test suite() {
	return new TestSuite(IFolderTest.class);
}
protected void tearDown() throws Exception {
	getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
	ensureDoesNotExistInWorkspace(getWorkspace().getRoot());
	super.tearDown();
}
public void testFolderCreation() throws Exception {
	// basic folder creation
	IProject project = getWorkspace().getRoot().getProject("Project");
	ensureExistsInWorkspace(project, true);

	IFolder target = project.getFolder("Folder1");
	try {
		assertTrue("1.0", !target.exists());
		target.create(true, true, getMonitor());
		assertTrue("1.1", target.exists());
	} catch (CoreException e) {
		fail("1.2", e);
	}

	// nested folder creation
	IFolder nestedTarget = target.getFolder("Folder2");
	try {
		assertTrue("2.0", !nestedTarget.exists());
		nestedTarget.create(true, true, getMonitor());
		assertTrue("2.1", nestedTarget.exists());
	} catch (CoreException e) {
		fail("2.2", e);
	}

	// try to create a folder that already exists
	try {
		assertTrue("3.0", target.exists());
		target.create(true, true, getMonitor());
		fail("3.1");
	} catch (CoreException e) {
		assertTrue("3.2", target.exists());
	}

	// try to create a folder over a file that exists
	IFile file = target.getFile("File1");
	target = target.getFolder("File1");
	try {
		file.create(getRandomContents(), true, getMonitor());
		assertTrue("4.0", file.exists());
	} catch (CoreException e) {
		fail("4.1", e);
	}

	try {
		target.create(true, true, getMonitor());
		fail("5.0");
	} catch (CoreException e) {
		assertTrue("5.1", file.exists());
		assertTrue("5.2", !target.exists());
	}

	// try to create a folder on a project (one segment) path
	try {
		target = getWorkspace().getRoot().getFolder(new Path("/Folder3"));
		fail("6.0");
	} catch (IllegalArgumentException e) {
	}

	// try to create a folder as a child of a file
	file = project.getFile("File2");
	try {
		file.create(null, true, getMonitor());
	} catch (CoreException e) {
		fail("7.0", e);
	}

	target = project.getFolder("File2/Folder4");
	try {
		assertTrue("7.1", !target.exists());
		target.create(true, true, getMonitor());
		fail("7.2");
	} catch (CoreException e) {
		assertTrue("7.3", file.exists());
		assertTrue("7.4", !target.exists());
	}

	// try to create a folder under a non-existant parent
	IFolder folder = project.getFolder("Folder5");
	target = folder.getFolder("Folder6");
	try {
		assertTrue("8.0", !folder.exists());
		target.create(true, true, getMonitor());
		fail("8.1");
	} catch (CoreException e) {
		assertTrue("8.2", !folder.exists());
		assertTrue("8.3", !target.exists());
	}
}
public void testFolderDeletion() throws Throwable {
	IProject project = getWorkspace().getRoot().getProject("Project");
	IResource[] before = buildResources(project, new String[] {"c/", "c/b/", "c/x", "c/b/y", "c/b/z"});
	ensureExistsInWorkspace(before, true);
	//
	assertExistsInWorkspace(before);
	project.getFolder("c").delete(true, getMonitor());
	assertDoesNotExistInWorkspace("1.0", before);
}
public void testFolderMove() throws Throwable {
	IProject project = getWorkspace().getRoot().getProject("Project");
	IResource[] before = buildResources(project, new String[] {"b/", "b/b/", "b/x", "b/b/y", "b/b/z"});
	IResource[] after = buildResources(project, new String[] {"a/", "a/b/", "a/x", "a/b/y", "a/b/z"});

	// create the resources and set some content in a file that will be moved.
	ensureExistsInWorkspace(before, true);
	String content = getRandomString();
	IFile file = project.getFile(new Path("b/b/z"));
	file.setContents(getContents(content), true, false, getMonitor());

	// Be sure the resources exist and then move them.
	assertExistsInWorkspace("1.0", before);
	project.getFolder("b").move(project.getFullPath().append("a"), true, getMonitor());

	//
	assertDoesNotExistInWorkspace("2.0", before);
	assertExistsInWorkspace(after);
	file = project.getFile(new Path("a/b/z"));
	assertTrue("2.1", compareContent(getContents(content), file.getContents(false)));
}
public void testFolderOverFile() throws Throwable {
	IPath path = new Path("/Project/File");
	IFile existing = getWorkspace().getRoot().getFile(path);
	ensureExistsInWorkspace(existing, true);
	IFolder target = getWorkspace().getRoot().getFolder(path);
	try {
		target.create(true, true, getMonitor());
		fail("1.0 Should not be able to create folder over a file");
	} catch (CoreException e) {
		assertTrue("2.0", existing.exists());
	}
}
/**
 * Tests creation and manipulation of folder names that are reserved on some platforms.
 */
public void testInvalidFolderNames() {
	IProject project = getWorkspace().getRoot().getProject("Project");
	ensureExistsInWorkspace(project, true);
	
	//do some tests with invalid names
	String[] names = new String[0];
	if (BootLoader.getOS().equals(BootLoader.OS_WIN32)) {
		//invalid windows names
		names = new String[] {"prn", "nul", "con", "aux", "clock$", "com1", 
			"com2", "com3", "com4", "com5", "com6", "com7", "com8", "com9",
			"lpt1", "lpt2", "lpt3", "lpt4", "lpt5", "lpt6", "lpt7", "lpt8", "lpt9",
			"AUX", "con.foo", "LPT4.txt", ":", "*", "?", "\"", "<", ">", "|"};
	} else {
		//invalid names on non-windows platforms
		names = new String[] {":"};
	}

	for (int i = 0; i < names.length; i++) {
		IFolder folder = project.getFolder(names[i]);
		assertTrue("1.0 " + names[i], !folder.exists());
		try {
			folder.create(true, true, getMonitor());
			fail("1.1 " + names[i]);
		} catch (CoreException e) {
		}
		assertTrue("1.2 " + names[i], !folder.exists());		
	}
		
	//do some tests with valid names that are *almost* invalid
	if (BootLoader.getOS().equals(BootLoader.OS_WIN32)) {
		//these names are valid on windows
		names = new String[] {"hello.prn.txt", "null", "con3", "foo.aux", "lpt0",
			"com0", "com10", "lpt10", ",", "'", ";"};
	} else {
		//these names are valid on non-windows platforms
		names = new String[] {"prn", "nul", "con", "aux", "clock$", 
			"com1", "com2", "com3", "com4", "com5", "com6", "com7", 
			"com8", "com9", "lpt1", "lpt2", "lpt3", "lpt4", "lpt5", "lpt6", 
			"lpt7", "lpt8", "lpt9", "AUX", "con.foo", "LPT4.txt", "*", 
			"?", "\"", "<", ">", "|", "hello.prn.txt", "null", "con3", "foo.aux", 
			"lpt0", "com0", "com10", "lpt10", ",", "'", ";"};
	}
	for (int i = 0; i < names.length; i++) {
		IFolder folder = project.getFolder(names[i]);
		assertTrue("2.0 " + names[i], !folder.exists());
		try {
			folder.create(true, true, getMonitor());
		} catch (CoreException e) {
			fail("2.1 " + names[i], e);
		}
		assertTrue("2.2 " + names[i], folder.exists());		
	}
}

public void testLeafFolderMove() throws Exception {
	IProject project = getWorkspace().getRoot().getProject("Project");
	IFolder source = project.getFolder("Folder1");
	ensureExistsInWorkspace(source, true);
	IFolder dest = project.getFolder("Folder2");
	source.move(dest.getFullPath(), true, getMonitor());
	assertExistsInWorkspace("1.0", dest);
	assertDoesNotExistInWorkspace("1.1", source);
}
public void testReadOnlyFolderCopy() throws Exception {
	IProject project = getWorkspace().getRoot().getProject("Project");
	IFolder source = project.getFolder("Folder1");
	ensureExistsInWorkspace(source, true);
	source.setReadOnly(true);
	IFolder dest = project.getFolder("Folder2");
	source.copy(dest.getFullPath(), true, getMonitor());
	assertExistsInWorkspace("1.0", dest);
	assertExistsInWorkspace("1.1", source);
	//XXX commented out pending fix for bug 6058.
//	assertTrue("1.2", dest.isReadOnly());
}
public void testSetGetFolderPersistentProperty() throws Throwable {
	IResource target = getWorkspace().getRoot().getFolder(new Path("/Project/Folder"));
	String value = "this is a test property value";
	QualifiedName name = new QualifiedName("itp-test", "testProperty");
	// getting/setting persistent properties on non-existent resources should throw an exception
	ensureDoesNotExistInWorkspace(target);
	try {
		target.getPersistentProperty(name);
		fail("1.0");
	} catch (CoreException e) {
		//this should happen
	}
	try {
		target.setPersistentProperty(name, value);
		fail("1.1");
	} catch (CoreException e) {
		//this should happen
	}
	
	ensureExistsInWorkspace(target, true);
	target.setPersistentProperty(name, value);
	// see if we can get the property
	assertTrue("2.0", ((String) target.getPersistentProperty(name)).equals(value));
	// see what happens if we get a non-existant property
	name = new QualifiedName("itp-test", "testNonProperty");
	assertNull("2.1", target.getPersistentProperty(name));
}
}
