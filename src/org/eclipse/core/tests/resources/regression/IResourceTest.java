package org.eclipse.core.tests.resources.regression;

import org.eclipse.core.boot.BootLoader;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.tests.harness.EclipseWorkspaceTest;
import java.io.InputStream;
import java.io.IOException;
import junit.framework.*;
import junit.textui.TestRunner;

public class IResourceTest extends EclipseWorkspaceTest {
public IResourceTest() {
}
public IResourceTest(String name) {
	super(name);
}
public static Test suite() {
	return new TestSuite(IResourceTest.class);
}
protected void tearDown() throws Exception {
	super.tearDown();
	getWorkspace().getRoot().delete(true, null);
}
/**
 * 1G9RBH5: ITPCORE:WIN98 - IFile.appendContents might lose data
 */
public void testAppendContents_1G9RBH5() {
	IProject project = getWorkspace().getRoot().getProject("MyProject");
	try {
		project.create(null);
		project.open(null);
	} catch (CoreException e) {
		fail("0.0", e);
	}

	IFile target = project.getFile("file1");
	try {
		target.create(getContents("abc"), false, null);
	} catch (CoreException e) {
		fail("1.0", e);
	}

	try {
		target.appendContents(getContents("def"), false, true, null);
	} catch (CoreException e) {
		fail("2.0", e);
	}

	InputStream content = null;
	try {
		content = target.getContents(false);
		assertTrue("3.0", compareContent(content, getContents("abcdef")));
	} catch (CoreException e) {
		fail("3.1", e);
	}
	
	// clean up
	try {
		project.delete(true, true, null);
	} catch (CoreException e) {
		fail("3.0", e);
	}
}
/**
 * 1GA6QJP: ITPCORE:ALL - Copying a resource does not copy its lastmodified time
 */
public void testCopy_1GA6QJP() {
	IProject project = getWorkspace().getRoot().getProject("MyProject");
	IFile source = project.getFile("file1");
	try {
		project.create(getMonitor());
		project.open(getMonitor());
		source.create(getContents("abc"), true, getMonitor());
	} catch (CoreException e) {
		fail("0.0", e);
	}

	try {
		Thread.sleep(5000);
	} catch (InterruptedException e) {
	}

	IPath destinationPath = new Path("copy of file");
	try {
		source.copy(destinationPath, true, getMonitor());
	} catch (CoreException e) {
		fail("1.0", e);
	}

	IFile destination = project.getFile(destinationPath);
	assertEquals("2.0", source.getLocation().toFile().lastModified(), destination.getLocation().toFile().lastModified());

	// clean up
	try {
		project.delete(true, true, null);
	} catch (CoreException e) {
		fail("3.0", e);
	}
}
/**
 * 1FW87XF: ITPUI:WIN2000 - Can create 2 files with same name
 */
public void testCreate_1FW87XF() {
	// FIXME: remove when fix this PR
	String os = BootLoader.getOS();
	if (!os.equals(BootLoader.OS_LINUX)) {
		log("Skipping testCreate_1FW87XF because it is still not supported by the platform.");
		return;
	}
	
	// test if the file system is case sensitive
	boolean caseSensitive = new java.io.File("abc").compareTo(new java.io.File("ABC")) != 0;

	IProject project = getWorkspace().getRoot().getProject("MyProject");
	IFile file = project.getFile("file");
	try {
		project.create(null);
		project.open(null);
		file.create(getRandomContents(), true, null);
	} catch (CoreException e) {
		fail("1.0", e);
	}

	// force = true
	assertTrue("2.0", file.exists());
	IFile anotherFile = project.getFile("File");
	try {
		anotherFile.create(getRandomContents(), true, null);
		if (!caseSensitive)
			fail("2.1");
	} catch (CoreException e) {
		if (caseSensitive)
			fail("2.2", e);
	}

	// clean-up
	try {
		anotherFile.delete(true, false, null);
	} catch (CoreException e) {
		fail("3.0", e);
	}

	// force = false
	try {
		anotherFile.create(getRandomContents(), false, null);
		if (!caseSensitive)
			fail("4.0");
	} catch (CoreException e) {
		if (caseSensitive)
			fail("4.1", e);
	}

	// test refreshLocal
	try {
		anotherFile.refreshLocal(IResource.DEPTH_ZERO, getMonitor());
		if (!caseSensitive)
			fail("5.0");
	} catch (CoreException e) {
		if (caseSensitive)
			fail("5.1", e);
	}

	// clean up
	try {
		project.delete(true, true, getMonitor());
	} catch (CoreException e) {
		fail("6.0", e);
	}
}
/**
 * 1FWYTKT: ITPCORE:WINNT - Error creating folder with long name
 */
public void testCreate_1FWYTKT() {
	IProject project = getWorkspace().getRoot().getProject("MyProject");
	try {
		project.create(null);
		project.open(null);
	} catch (CoreException e) {
		fail("1.0", e);
	}

	StringBuffer sb = new StringBuffer();
	for (int i = 0; i < 260; i++)
		sb.append('a');
	sb.append('b');
	IFolder folder = project.getFolder(sb.toString());
	try {
		folder.create(true, true, null);
		fail("2.1");
	} catch (CoreException e) {
	}
	assertTrue("2.2", !folder.exists());

	IFile file = project.getFile(sb.toString());
	try {
		file.create(getRandomContents(), true, null);
		fail("3.0");
	} catch (CoreException e) {
	}
	assertTrue("3.1", !file.exists());

	// clean up
	try {
		project.delete(true, true, null);
	} catch (CoreException e) {
		fail("3.0", e);
	}

	project = getWorkspace().getRoot().getProject(sb.toString());
	try {
		project.create(null);
		fail("4.0");
	} catch (CoreException e) {
	}
	assertTrue("4.1", !project.exists());
}
/**
 * 1GD7CSU: ITPCORE:ALL - IFile.create bug?
 * 
 * Ensure that creating a file with force==true doesn't throw
 * a CoreException if the resource already exists on disk.
 */
public void testCreate_1GD7CSU() {
	IProject project = getWorkspace().getRoot().getProject("MyProject");
	try {
		project.create(null);
		project.open(null);
	} catch (CoreException e) {
		fail("1.0", e);
	}

	IFile file = project.getFile("MyFile");
	ensureExistsInFileSystem(file);
	
	try {
		file.create(getRandomContents(), true, getMonitor());
	} catch (CoreException e) {
		fail("2.0", e);
	}
}
/*
 * Test PR: 1GD3ZUZ. Ensure that a CoreException is being thrown
 * when we try to delete a read-only resource. It will depend on the
 * OS and file system.
 */
public void testDelete_1GD3ZUZ() {
	// This test cannot be done automatically because we don't know in that
	// file system we are running. Will leave test here in case it needs
	// to be run it in a special environment.
	if (true)
		return;
	
	IProject project = getWorkspace().getRoot().getProject("MyProject");
	IFile file = project.getFile("MyFile");
	
	// setup
	ensureExistsInWorkspace(new IResource[] {project, file}, true);
	file.setReadOnly(true);
	assertTrue("2.0", file.isReadOnly());
	
	// doit
	try {
		file.delete(false, getMonitor());
		fail("3.0");
	} catch (CoreException e) {
	}
	
	// cleanup
	file.setReadOnly(false);
	assertTrue("4.0", !file.isReadOnly());
	ensureDoesNotExistInWorkspace(new IResource[] {project, file});
}
public void testDelete_Bug8754() {
	//In this test, we delete with force false on a file that does not exist in the file system,
	//and ensure that the returned exception is of type OUT_OF_SYNC_LOCAL
	
	IProject project = getWorkspace().getRoot().getProject("MyProject");
	IFile file = project.getFile("MyFile");
	
	// setup
	ensureExistsInWorkspace(new IResource[] {project, file}, true);
	ensureOutOfSync(file);
	
	// doit
	try {
		file.delete(false, getMonitor());
		fail("1.0");
	} catch (CoreException e) {
		IStatus status = e.getStatus();
		if (status.isMultiStatus()) {
			IStatus[] children = status.getChildren();
			assertEquals("1.1", 1, children.length);
			status = children[0];
		}
		assertEquals("1.2", IResourceStatus.OUT_OF_SYNC_LOCAL, status.getCode());
	}
	//cleanup
	ensureDoesNotExistInWorkspace(new IResource[] {project, file});
}
public void testEquals_1FUOU25() {
	IResource fileResource = getWorkspace().getRoot().getFile(new Path("a/b/c/d"));
	IResource folderResource = getWorkspace().getRoot().getFolder(new Path("a/b/c/d"));
	assertTrue("1FUOU25: ITPCORE:ALL - Bug in Resource.equals()", !fileResource.equals(folderResource));
}
public void testExists_1FUP8U6() {
	IProject project = getWorkspace().getRoot().getProject("MyProject");
	IFolder folder = project.getFolder("folder");
	try {
		project.create(null);
		project.open(null);
		folder.create(true, true, null);
	} catch (CoreException e) {
		fail("1.0", e);
	}
	IFile file = project.getFile("folder");
	assertTrue("2.0", !file.exists());
	
	// clean up
	try {
		project.delete(true, true, null);
	} catch (CoreException e) {
		fail("3.0", e);
	}
}
/**
 * 1GA6QYV: ITPCORE:ALL - IContainer.findMember( Path, boolean ) breaking API
 */
public void testFindMember_1GA6QYV() {
	IProject project = getWorkspace().getRoot().getProject("MyProject");
	try {
		project.create(getMonitor());
		project.open(getMonitor());
	} catch (CoreException e) {
		fail("1.0", e);
	}

	IFolder folder1 = project.getFolder("Folder1");
	IFolder folder2 = folder1.getFolder("Folder2");
	IFolder folder3 = folder2.getFolder("Folder3");
	try {
		folder1.create(true, true, getMonitor());
		folder2.create(true, true, getMonitor());
		folder3.create(true, true, getMonitor());
	} catch (CoreException e) {
		fail("2.0", e);
	}

	IPath targetPath = new Path("Folder2/Folder3");
	IFolder target = (IFolder) folder1.findMember(targetPath);
	assertTrue("3.0", folder3.equals(target));

	targetPath = new Path("/Folder2/Folder3");
	target = (IFolder) folder1.findMember(targetPath);
	assertTrue("4.0", folder3.equals(target));

	// clean up
	try {
		project.delete(true, true, null);
	} catch (CoreException e) {
		fail("3.0", e);
	}
}
/**
 * 1GBZD4S: ITPCORE:API - IFile.getContents(true) fails if performed during delta notification
 */
public void testGetContents_1GBZD4S() {
	IProject project = getWorkspace().getRoot().getProject("MyProject");
	try {
		project.create(null);
		project.open(null);
	} catch (CoreException e) {
		fail("0.0", e);
	}

	final IFile target = project.getFile("file1");
	String contents = "some random contents";
	try {
		target.create(getContents(contents), false, null);
	} catch (CoreException e) {
		fail("1.0", e);
	}

	try {
		InputStream is = target.getContents(false);
		assertTrue("2.0", compareContent(getContents(contents), is));
	} catch (CoreException e) {
		fail("2.1", e);
	}

	final String newContents = "some other contents";
	try {
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
		}
		createFileInFileSystem(target.getLocation(), getContents(newContents));
	} catch (IOException e) {
		fail("3.0", e);
	}

	final boolean[] failed = new boolean[1];
	IResourceChangeListener listener = new IResourceChangeListener() {
		public void resourceChanged(IResourceChangeEvent event) {
			try {
				failed[0] = true;
				InputStream is = target.getContents(true);
				assertTrue("4.0", compareContent(getContents(newContents), is));
				failed[0] = false;
			} catch (CoreException e) {
				fail("4.1", e);
			}
		}
	};
	getWorkspace().addResourceChangeListener(listener);
	// trigger delta notification
	try {
		project.touch(null);
	} catch (CoreException e) {
		fail("4.5", e);
	}
	getWorkspace().removeResourceChangeListener(listener);
	assertTrue("4.6", !failed[0]);

	try {
		target.getContents(false);
		fail("5.0");
	} catch (CoreException e) {
	}

	try {
		InputStream is = target.getContents(true);
		assertTrue("6.0", compareContent(getContents(newContents), is));
	} catch (CoreException e) {
		fail("6.1", e);
	}

	// clean up
	try {
		project.delete(true, true, null);
	} catch (CoreException e) {
		fail("3.0", e);
	}
}
/**
 * 1G60AFG: ITPCORE:WIN - problem calling RefreshLocal with DEPTH_ZERO on folder
 */
public void testRefreshLocal_1G60AFG() {
	IProject project = getWorkspace().getRoot().getProject("MyProject");
	IFolder folder = project.getFolder("folder");
	IFile file = folder.getFile("file");
	try {
		project.create(null);
		project.open(null);
		folder.create(true, true, null);
		file.create(getRandomContents(), true, null);
	} catch (CoreException e) {
		fail("1.0", e);
	}

	assertTrue("2.0", file.exists());
	try {
		folder.refreshLocal(IResource.DEPTH_ZERO, null);
	} catch (CoreException e) {
		fail("2.1", e);
	}
	assertTrue("2.2", file.exists());

	// clean up
	try {
		project.delete(true, true, null);
	} catch (CoreException e) {
		fail("3.0", e);
	}
}
}
