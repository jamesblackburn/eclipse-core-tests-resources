package org.eclipse.core.tests.internal.localstore;

import org.eclipse.core.runtime.*;
import org.eclipse.core.internal.localstore.*;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.tests.harness.EclipseWorkspaceTest;
import java.io.*;
import junit.framework.*;
//
public class SafeFileInputOutputStreamTest extends EclipseWorkspaceTest {
	protected File temp;
public SafeFileInputOutputStreamTest() {
	super();
}
public SafeFileInputOutputStreamTest(String name) {
	super(name);
}
public SafeFileOutputStream createSafeStream(File target, String errorCode) {
	return createSafeStream(target.getAbsolutePath(), null, errorCode);
}
public SafeFileOutputStream createSafeStream(String targetPath, String tempFilePath, String errorCode) {
	try {
		return new SafeFileOutputStream(targetPath, tempFilePath);
	} catch (IOException e) {
		fail(errorCode, e);
	}
	return null; // never happens
}
public InputStream getContents(java.io.File target, String errorCode) {
	try {
		return new SafeFileInputStream(target);
	} catch (IOException e) {
		fail(errorCode, e);
	}
	return null; // never happens
}
protected void setUp() throws Exception {
	super.setUp();
	Path location = new Path(System.getProperty("user.dir"));
	temp = location.append("temp").toFile();
	temp.mkdirs();
	assertTrue("could not create temp directory", temp.isDirectory());
}
public static Test suite() {
	return new TestSuite(SafeFileInputOutputStreamTest.class);
}
protected void tearDown() throws Exception {
	Workspace.clear(temp);
	super.tearDown();
}
public void testSafeFileInputStream() {
	File target = new File(temp, "target");
	Workspace.clear(target); // make sure there was nothing here before
	assertTrue("1.0", !target.exists());
	FileSystemStore store = new FileSystemStore();

	// define temp path
	Path parentLocation = new Path(target.getParentFile().getAbsolutePath());
	IPath tempLocation = parentLocation.append(target.getName() + ".backup");

	// we did not have a file on the destination, so we should not have a temp file
	SafeFileOutputStream safeStream = createSafeStream(target.getAbsolutePath(), tempLocation.toOSString(), "2.0");
	File tempFile = tempLocation.toFile();
	String contents = getRandomString();
	try {
		store.transferStreams(getContents(contents), safeStream, target.getAbsolutePath(), null);
	} catch (CoreException e) {
		fail("3.0", e);
	}

	// now we should have a temp file
	safeStream = createSafeStream(target.getAbsolutePath(), tempLocation.toOSString(), "4.0");
	tempFile = tempLocation.toFile();
	try {
		store.transferStreams(getContents(contents), safeStream, target.getAbsolutePath(), null);
	} catch (CoreException e) {
		fail("4.0", e);
	}

	assertTrue("5.0", target.exists());
	assertTrue("5.1", !tempFile.exists());
	InputStream diskContents;
	try {
		diskContents = new SafeFileInputStream(tempLocation.toOSString(), target.getAbsolutePath());
		assertTrue("5.2", compareContent(diskContents, getContents(contents)));
	} catch (IOException e) {
		fail("5.3", e);
	}
	Workspace.clear(target); // make sure there was nothing here before
}
public void testSimple() {
	File target = new File(temp, "target");
	Workspace.clear(target); // make sure there was nothing here before
	assertTrue("1.0", !target.exists());
	FileSystemStore store = new FileSystemStore();

	// basic use (like a FileOutputStream)
	SafeFileOutputStream safeStream = createSafeStream(target, "1.0");
	String contents = getRandomString();
	try {
		store.transferStreams(getContents(contents), safeStream, target.getAbsolutePath(), null);
	} catch (CoreException e) {
		fail("1.1", e);
	}
	InputStream diskContents = getContents(target, "1.2");
	assertTrue("1.3", compareContent(diskContents, getContents(contents)));

	// update target contents
	contents = getRandomString();
	safeStream = createSafeStream(target, "2.0");
	File tempFile = new File(safeStream.getTempFilePath());
	assertTrue("2.0", tempFile.exists());
	try {
		store.transferStreams(getContents(contents), safeStream, target.getAbsolutePath(), null);
	} catch (CoreException e) {
		fail("3.0", e);
	}
	diskContents = getContents(target, "3.1");
	assertTrue("3.2", compareContent(diskContents, getContents(contents)));
	assertTrue("3.3", !tempFile.exists());
	Workspace.clear(target); // make sure there was nothing here before
}
public void testSpecifiedTempFile() {
	File target = new File(temp, "target");
	Workspace.clear(target); // make sure there was nothing here before
	assertTrue("1.0", !target.exists());
	FileSystemStore store = new FileSystemStore();

	// define temp path
	Path parentLocation = new Path(target.getParentFile().getAbsolutePath());
	IPath tempLocation = parentLocation.append(target.getName() + ".backup");

	// we did not have a file on the destination, so we should not have a temp file
	SafeFileOutputStream safeStream = createSafeStream(target.getAbsolutePath(), tempLocation.toOSString(), "2.0");
	File tempFile = tempLocation.toFile();
	assertTrue("2.1", !tempFile.exists());

	// update target contents
	String contents = getRandomString();
	try {
		store.transferStreams(getContents(contents), safeStream, target.getAbsolutePath(), null);
	} catch (CoreException e) {
		fail("3.0", e);
	}
	InputStream diskContents = getContents(target, "3.1");
	assertTrue("3.2", compareContent(diskContents, getContents(contents)));
	assertTrue("3.3", !tempFile.exists());
	
	// now we should have a temp file
	safeStream = createSafeStream(target.getAbsolutePath(), tempLocation.toOSString(), "4.0");
	tempFile = tempLocation.toFile();
	assertTrue("4.1", tempFile.exists());

	// update target contents
	contents = getRandomString();
	try {
		store.transferStreams(getContents(contents), safeStream, target.getAbsolutePath(), null);
	} catch (CoreException e) {
		fail("5.0", e);
	}
	diskContents = getContents(target, "5.1");
	assertTrue("5.2", compareContent(diskContents, getContents(contents)));
	assertTrue("5.3", !tempFile.exists());
	Workspace.clear(target); // make sure there was nothing here before
}
}
