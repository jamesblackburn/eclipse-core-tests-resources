/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.resources;

import java.io.*;
import java.util.Enumeration;
import java.util.Vector;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.boot.BootLoader;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.tests.harness.EclipseWorkspaceTest;
import org.eclipse.core.tests.harness.FussyProgressMonitor;
import org.eclipse.osgi.service.environment.Constants;

public class IFileTest extends EclipseWorkspaceTest {
	IProject[] projects = null;

	Vector allFiles = new Vector();
	Vector localOnlyFiles = new Vector();
	Vector workspaceOnlyFiles = new Vector();
	Vector nonExistingFiles = new Vector();
	Vector existingFiles = new Vector();
	Vector outOfSyncFiles = new Vector();

	//name of files according to sync category
	public static final String DOES_NOT_EXIST = "DoesNotExistFile";
	public static final String LOCAL_ONLY = "LocalOnlyFile";
	public static final String EXISTING = "ExistingFile";
	public static final String WORKSPACE_ONLY = "WorkspaceOnlyFile";
	public static final String OUT_OF_SYNC = "OutOfSyncFile";

	protected static final Boolean[] TRUE_AND_FALSE = new Boolean[] {Boolean.TRUE, Boolean.FALSE};
	//	protected static final IProgressMonitor[] PROGRESS_MONITORS = new IProgressMonitor[] {new FussyProgressMonitor(), new CancelingProgressMonitor(), null};
	protected static final IProgressMonitor[] PROGRESS_MONITORS = new IProgressMonitor[] {new FussyProgressMonitor(), null};

	public IFileTest() {
		super();
	}

	public IFileTest(String name) {
		super(name);
	}

	/**
	 * Returns true if the given container exists, and is open
	 * if applicable.
	 */
	public boolean existsAndOpen(IContainer container) {
		if (!container.exists())
			return false;
		if (container instanceof IFolder) {
			return true;
		}
		if (container instanceof IProject) {
			return ((IProject) container).isOpen();
		}
		fail("Should not get here in FileTest.existsAndOpen");
		return false;
	}

	/**
	 * This method creates the necessary resources
	 * for the FileTests.  The interesting files are
	 * placed in vectors that are members of the class.
	 */
	protected void generateInterestingFiles() throws CoreException {
		IProject[] projects = interestingProjects();
		for (int i = 0; i < projects.length; i++) {
			IProject project = projects[i];
			//file in project
			generateInterestingFiles(project);

			//file in non-existent folder
			generateInterestingFiles(project.getFolder("NonExistentFolder"));

			//file in existent folder
			if (project.exists() && project.isOpen()) {
				IFolder folder = project.getFolder("ExistingFolder");
				folder.create(true, true, getMonitor());
				generateInterestingFiles(folder);
			}
		}
	}

	/**
	 * Creates some interesting files in the specified container.
	 * Adds these files to the appropriate member vectors.
	 * Conditions on these files (out of sync, workspace only, etc)
	 * will be ensured by refreshFiles
	 */
	public void generateInterestingFiles(IContainer container) {
		//non-existent file
		IFile file = container.getFile(new Path(DOES_NOT_EXIST));
		nonExistingFiles.addElement(file);
		allFiles.addElement(file);

		//exists in filesystem only
		file = container.getFile(new Path(LOCAL_ONLY));
		localOnlyFiles.addElement(file);
		allFiles.addElement(file);

		if (existsAndOpen(container)) {

			//existing file
			file = container.getFile(new Path(EXISTING));
			existingFiles.addElement(file);
			allFiles.addElement(file);

			//exists in workspace only
			file = container.getFile(new Path(WORKSPACE_ONLY));
			workspaceOnlyFiles.addElement(file);
			allFiles.addElement(file);

			//exists in both but is out of sync
			file = container.getFile(new Path(OUT_OF_SYNC));
			outOfSyncFiles.addElement(file);
			allFiles.addElement(file);
		}
	}

	/**
	 * Returns some interesting files.  These files are created
	 * during setup.
	 */
	public IFile[] interestingFiles() {
		refreshFiles();
		IFile[] result = new IFile[allFiles.size()];
		allFiles.copyInto(result);
		return result;
	}

	/**
	 * Creates and returns some interesting projects
	 */
	public IProject[] interestingProjects() throws CoreException {
		if (projects == null) {
			projects = new IProject[3];

			//open project
			IProject openProject = getWorkspace().getRoot().getProject("OpenProject");
			openProject.create(getMonitor());
			openProject.open(getMonitor());
			projects[0] = openProject;

			//closed project
			IProject closedProject = getWorkspace().getRoot().getProject("ClosedProject");
			closedProject.create(getMonitor());
			projects[1] = closedProject;

			//non-existent project
			projects[2] = getWorkspace().getRoot().getProject("NonExistentProject");
		}

		return projects;
	}

	/**
	 * Returns some interesting input streams
	 */
	public InputStream[] interestingStreams() {
		Vector streams = new Vector();

		//empty stream
		ByteArrayInputStream bis = new ByteArrayInputStream(new byte[0]);
		streams.addElement(bis);

		// random content
		streams.addElement(getRandomContents());

		//large stream
		bis = new ByteArrayInputStream(new byte[10000]);
		streams.addElement(bis);

		InputStream[] results = new InputStream[streams.size()];
		streams.copyInto(results);
		return results;
	}

	/**
	 * Returns true if the given file is out of sync from the
	 * local file system.  The file must exist in the workspace.
	 */
	public boolean outOfSync(IFile file) {
		return file.getName().equals(OUT_OF_SYNC) || file.getName().equals(WORKSPACE_ONLY);
	}

	/**
	 * Makes sure file requirements are met (out of sync, workspace only, etc).
	 */
	public void refreshFile(IFile file) {
		if (file.getName().equals(LOCAL_ONLY)) {
			ensureDoesNotExistInWorkspace(file);
			//project must exist to access file system store.
			if (file.getProject().exists()) {
				ensureExistsInFileSystem(file);
			}
			return;
		}
		if (file.getName().equals(WORKSPACE_ONLY)) {
			ensureExistsInWorkspace(file, true);
			ensureDoesNotExistInFileSystem(file);
			return;
		}
		if (file.getName().equals(DOES_NOT_EXIST)) {
			ensureDoesNotExistInWorkspace(file);
			//project must exist to access file system store.
			if (file.getProject().exists()) {
				ensureDoesNotExistInFileSystem(file);
			}
			return;
		}
		if (file.getName().equals(EXISTING)) {
			ensureExistsInWorkspace(file, true);
			return;
		}
		if (file.getName().equals(OUT_OF_SYNC)) {
			ensureExistsInWorkspace(file, true);
			ensureOutOfSync(file);
			return;
		}
	}

	/**
	 * Makes sure file requirements are met (out of sync, workspace only, etc).
	 */
	public void refreshFiles() {
		for (Enumeration e = allFiles.elements(); e.hasMoreElements();) {
			IFile file = (IFile) e.nextElement();
			refreshFile(file);
		}
	}

	protected void setUp() throws Exception {
		super.setUp();

		try {
			generateInterestingFiles();
		} catch (CoreException e) {
			fail("Failed in setup for FileTest", e);
		}
	}

	/**
	 * Sets up the test suite for this class
	 */
	public static Test suite() {
		return new TestSuite(IFileTest.class);

		//	TestSuite suite = new TestSuite();
		//	suite.addTest(new IFileTest("testAppendContents2"));
		//	suite.addTest(new IFileTest("testSetContents3"));
		//	return suite;
	}

	protected void tearDown() throws Exception {
		getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
		ensureDoesNotExistInWorkspace(getWorkspace().getRoot());
		super.tearDown();
	}

	public void testAppendContents() {
		IFile target = projects[0].getFile("file1");
		try {
			target.create(getContents("abc"), false, null);
		} catch (CoreException e) {
			fail("1.0", e);
		}

		try {
			target.appendContents(getContents("def"), false, false, null);
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
	}

	/**
	 * Performs black box testing of the following method:
	 *     void create(InputStream, boolean, IProgressMonitor)
	 */
	public void testCreate() {
		Object[][] inputs = new Object[][] {interestingFiles(), interestingStreams(), TRUE_AND_FALSE, PROGRESS_MONITORS};
		new TestPerformer("IFileTest.testCreate") {
			public boolean shouldFail(Object[] args, int count) {
				IFile file = (IFile) args[0];
				IPath fileLocation = file.getLocation();
				boolean force = ((Boolean) args[2]).booleanValue();
				boolean fileExistsInWS = file.exists();
				boolean fileExistsInFS = fileLocation != null && fileLocation.toFile().exists();

				// parent must be accessible
				if (!file.getParent().isAccessible())
					return true;

				// should never fail if force is true
				if (force && !fileExistsInWS)
					return false;

				// file must not exist in WS or on filesystem.
				return fileExistsInWS || fileExistsInFS;
			}

			public Object[] interestingOldState(Object[] args) throws Exception {
				return null;
			}

			public Object invokeMethod(Object[] args, int count) throws Exception {
				IFile file = (IFile) args[0];
				InputStream stream = (InputStream) args[1];
				boolean force = ((Boolean) args[2]).booleanValue();
				IProgressMonitor monitor = (IProgressMonitor) args[3];
				if (monitor instanceof FussyProgressMonitor)
					((FussyProgressMonitor) monitor).prepare();
				file.create(stream, force, monitor);
				if (monitor instanceof FussyProgressMonitor)
					((FussyProgressMonitor) monitor).sanityCheck();
				return null;
			}

			public boolean wasSuccess(Object[] args, Object result, Object[] oldState) throws Exception {
				IFile file = (IFile) args[0];
				return file.exists();
			}

			public void cleanUp(Object[] args, int count) {
				IFile file = (IFile) args[0];
				refreshFile(file);
			}
		}.performTest(inputs);
	}

	public void testFileCreation() {
		IFile target = projects[0].getFile("file1");

		assertTrue("1.0", !target.exists());
		try {
			target.create(null, true, getMonitor());
		} catch (CoreException e) {
			fail("1.1", e);
		}
		assertTrue("1.2", target.exists());

		// creation with empty content
		target = projects[0].getFile("file2");
		assertTrue("2.0", !target.exists());
		String contents = "";
		try {
			target.create(getContents(contents), true, getMonitor());
		} catch (CoreException e) {
			fail("2.1", e);
		}
		assertTrue("2.2", target.exists());
		InputStream stream = null;
		try {
			stream = target.getContents(false);
		} catch (CoreException e) {
			fail("2.3", e);
		}
		try {
			assertTrue("2.4", stream.available() == 0);
		} catch (IOException e) {
			fail("2.5", e);
		}
		try {
			stream.close();
		} catch (IOException e) {
			fail("2.55", e);
		}
		try {
			assertTrue("2.6", compareContent(target.getContents(false), getContents(contents)));
		} catch (CoreException e) {
			fail("2.7", e);
		}

		// creation with random content
		target = projects[0].getFile("file3");
		assertTrue("3.0", !target.exists());
		contents = getRandomString();
		try {
			target.create(getContents(contents), true, getMonitor());
		} catch (CoreException e) {
			fail("3.1", e);
		}
		assertTrue("3.2", target.exists());
		try {
			assertTrue("3.2", compareContent(target.getContents(false), getContents(contents)));
		} catch (CoreException e) {
			fail("3.3", e);
		}

		// try to create a file over a folder that exists
		IFolder folder = projects[0].getFolder("folder1");
		try {
			folder.create(true, true, getMonitor());
		} catch (CoreException e) {
			fail("4.0", e);
		}
		assertTrue("4.1", folder.exists());
		target = projects[0].getFile("folder1");
		try {
			target.create(null, true, getMonitor());
			fail("4.2");
		} catch (CoreException e) {
			// expected
		}
		assertTrue("4.3", folder.exists());
		assertTrue("4.4", !target.exists());

		// try to create a file under a non-existent parent
		folder = projects[0].getFolder("folder2");
		assertTrue("5.0", !folder.exists());
		target = folder.getFile("file4");
		try {
			target.create(null, true, getMonitor());
			fail("5.1");
		} catch (CoreException e) {
			// expected
		}
		assertTrue("5.2", !folder.exists());
		assertTrue("5.3", !target.exists());

		//create from stream that throws exceptions
		target = projects[0].getFile("file2");
		ensureDoesNotExistInWorkspace(target);
		ensureDoesNotExistInFileSystem(target);

		InputStream content = new InputStream() {
			public int read() throws IOException {
				throw new IOException();
			}
		};
		try {
			target.create(content, false, getMonitor());
			fail("6.1");
		} catch (CoreException e) {
			// expected
		}
		assertDoesNotExistInWorkspace("6.2", target);
		assertDoesNotExistInFileSystem("6.3", target);

		// cleanup
		folder = projects[0].getFolder("folder1");
		try {
			folder.delete(false, getMonitor());
		} catch (CoreException e) {
			fail("7.0", e);
		}
		IFile file = projects[0].getFile("file1");
		try {
			file.delete(false, getMonitor());
		} catch (CoreException e) {
			fail("7.1", e);
		}
		file = projects[0].getFile("file2");
		try {
			file.delete(false, getMonitor());
		} catch (CoreException e) {
			fail("7.2", e);
		}
		file = projects[0].getFile("file3");
		try {
			file.delete(false, getMonitor());
		} catch (CoreException e) {
			fail("7.3", e);
		}
	}

	public void testFileDeletion() throws Throwable {
		IFile target = projects[0].getFile("file1");
		target.create(null, true, getMonitor());
		assertTrue("1.0", target.exists());
		target.delete(true, getMonitor());
		assertTrue("1.1", !target.exists());
	}

	public void testFileEmptyDeletion() throws Throwable {
		IFile target = projects[0].getFile("file1");
		target.create(getContents(""), true, getMonitor());
		assertTrue("1.0", target.exists());
		target.delete(true, getMonitor());
		assertTrue("1.1", !target.exists());
	}

	public void testFileInFolderCreation() {

		IFolder folder = projects[0].getFolder("folder1");
		try {
			folder.create(false, true, getMonitor());
		} catch (CoreException e) {
			fail("1.0", e);
		}

		IFile target = folder.getFile("file1");
		try {
			target.create(getRandomContents(), true, getMonitor());
			assertTrue("1.1", target.exists());
		} catch (CoreException e) {
			fail("1.2", e);
		}
	}

	public void testFileInFolderCreation1() throws Throwable {
		IFolder folder = projects[0].getFolder("folder1");
		folder.create(false, true, null);

		IFile target = folder.getFile("file1");
		target.create(getRandomContents(), true, getMonitor());
		assertTrue("1.0", target.exists());
	}

	public void testFileInFolderCreation2() {

		IFolder folder = projects[0].getFolder("folder1");
		try {
			folder.create(false, true, getMonitor());
		} catch (CoreException e) {
			fail("1.0", e);
		}

		IFile target = folder.getFile("file1");
		try {
			target.create(getRandomContents(), true, getMonitor());
			assertTrue("1.1", target.exists());
		} catch (CoreException e) {
			fail("1.2", e);
		}
	}

	public void testFileMove() throws Throwable {
		IFile target = projects[0].getFile("file1");
		target.create(getRandomContents(), true, getMonitor());

		IFile destination = projects[0].getFile("file2");
		target.move(destination.getFullPath(), true, getMonitor());

		assertTrue("1.0", destination.exists());
		assertTrue("1.1", !target.exists());
	}

	public void testFileOverFolder() throws Throwable {
		IFolder existing = projects[0].getFolder("ExistingFolder");
		IFile target = projects[0].getFile("ExistingFolder");

		try {
			target.create(null, true, getMonitor());
			fail("Should not be able to create file over folder");
		} catch (CoreException e) {
			assertTrue("1.1", existing.exists());
			return;
		}
	}

	/**
	 * Performs black box testing of the following method:
	 *     InputStream getContents()
	 */
	public void testGetContents() {
		Object[][] inputs = new Object[][] {interestingFiles()};
		new TestPerformer("IFileTest.testGetContents") {
			public boolean shouldFail(Object[] args, int count) {
				IFile file = (IFile) args[0];

				//file must exist
				if (!file.exists()) {
					return true;
				}

				//file must be in sync
				if (outOfSync(file)) {
					return true;
				}
				return false;
			}

			public Object[] interestingOldState(Object[] args) throws Exception {
				return null;
			}

			public Object invokeMethod(Object[] args, int count) throws Exception {
				IFile file = (IFile) args[0];
				return file.getContents(false);
			}

			public boolean wasSuccess(Object[] args, Object result, Object[] oldState) throws Exception {
				IFile file = (IFile) args[0];
				InputStream contents = (InputStream) result;
				boolean returnVal = file.exists() && contents != null;
				contents.close();
				return returnVal;
			}

			public void cleanUp(Object[] args, int count) {
				IFile file = (IFile) args[0];
				refreshFile(file);
			}
		}.performTest(inputs);
	}

	public void testGetContents2() throws IOException {
		IFile target = projects[0].getFile("file1");
		String testString = getRandomString();
		try {
			target.create(null, false, null);
			target.setContents(getContents(testString), true, false, getMonitor());
		} catch (CoreException e) {
			fail("0.0", e);
		}

		ensureOutOfSync(target);

		InputStream content = null;
		try {
			try {
				content = target.getContents(false);
			} finally {
				if (content != null)
					content.close();
			}
			fail("1.0");
		} catch (CoreException e) {
			// expected
		}

		try {
			try {
				content = target.getContents(true);
			} finally {
				if (content != null)
					content.close();
			}
		} catch (CoreException e) {
			fail("2.1", e);
		}

		try {
			try {
				content = target.getContents(false);
			} finally {
				if (content != null)
					content.close();
			}
			fail("3.0");
		} catch (CoreException e) {
			// expected
		}
		content = new InputStream() {
			public int read() throws IOException {
				throw new IOException();
			}
		};
		try {
			target.setContents(content, IResource.NONE, getMonitor());
			fail("4.1");
		} catch (CoreException e) {
			// expected
		}
		assertExistsInWorkspace("4.2", target);
		assertExistsInFileSystem("4.3", target);

	}

	/**
	 * Tests creation and manipulation of file names that are reserved on some platforms.
	 */
	public void testInvalidFileNames() {
		IProject project = projects[0];

		//should not be able to create a file with invalid path on any platform
		String[] names = new String[] {":", "", "/"};
		for (int i = 0; i < names.length; i++) {
			try {
				project.getFile(names[i]);
				fail("0.99");
			} catch (RuntimeException e) {
				//should fail
			}
		}

		//do some tests with invalid names
		names = new String[0];
		if (BootLoader.getOS().equals(Constants.OS_WIN32)) {
			//invalid windows names
			names = new String[] {"prn", "nul", "con", "aux", "clock$", "com1", "com2", "com3", "com4", "com5", "com6", "com7", "com8", "com9", "lpt1", "lpt2", "lpt3", "lpt4", "lpt5", "lpt6", "lpt7", "lpt8", "lpt9", "AUX", "con.foo", "LPT4.txt", "*", "?", "\"", "<", ">", "|"};
		} else {
			//invalid names on non-windows platforms
			names = new String[] {};
		}
		for (int i = 0; i < names.length; i++) {
			IFile file = project.getFile(names[i]);
			assertTrue("1.0 " + names[i], !file.exists());
			try {
				file.create(getRandomContents(), true, getMonitor());
				fail("1.1 " + names[i]);
			} catch (CoreException e) {
				// expected
			}
			assertTrue("1.2 " + names[i], !file.exists());
		}

		//do some tests with valid names that are *almost* invalid
		if (BootLoader.getOS().equals(Constants.OS_WIN32)) {
			//these names are valid on windows
			names = new String[] {"hello.prn.txt", "null", "con3", "foo.aux", "lpt0", "com0", "com10", "lpt10", ",", "'", ";"};
		} else {
			//these names are valid on non-windows platforms
			names = new String[] {"prn", "nul", "con", "aux", "clock$", "com1", "com2", "com3", "com4", "com5", "com6", "com7", "com8", "com9", "lpt1", "lpt2", "lpt3", "lpt4", "lpt5", "lpt6", "lpt7", "lpt8", "lpt9", "AUX", "con.foo", "LPT4.txt", "*", "?", "\"", "<", ">", "|", "hello.prn.txt", "null", "con3", "foo.aux", "lpt0", "com0", "com10", "lpt10", ",", "'", ";"};
		}
		for (int i = 0; i < names.length; i++) {
			IFile file = project.getFile(names[i]);
			assertTrue("2.0 " + names[i], !file.exists());
			try {
				file.create(getRandomContents(), true, getMonitor());
			} catch (CoreException e) {
				fail("2.1 " + names[i], e);
			}
			assertTrue("2.2 " + names[i], file.exists());
		}
	}

	/**
	 * Performs black box testing of the following method:
	 *     void setContents(InputStream, boolean, IProgressMonitor)
	 */
	public void testSetContents1() {
		Object[][] inputs = new Object[][] {interestingFiles(), interestingStreams(), TRUE_AND_FALSE, PROGRESS_MONITORS};
		new TestPerformer("IFileTest.testSetContents1") {
			public boolean shouldFail(Object[] args, int count) {
				IFile file = (IFile) args[0];
				boolean force = ((Boolean) args[2]).booleanValue();

				//file must exist
				if (!file.exists()) {
					return true;
				}

				//file must be in sync if force is false
				if (!force && outOfSync(file)) {
					return true;
				}
				return false;
			}

			public Object[] interestingOldState(Object[] args) throws Exception {
				return null;
			}

			public Object invokeMethod(Object[] args, int count) throws Exception {
				IFile file = (IFile) args[0];
				InputStream stream = (InputStream) args[1];
				boolean force = ((Boolean) args[2]).booleanValue();
				IProgressMonitor monitor = (IProgressMonitor) args[3];
				if (monitor instanceof FussyProgressMonitor)
					((FussyProgressMonitor) monitor).prepare();
				file.setContents(stream, force, false, monitor);
				if (monitor instanceof FussyProgressMonitor)
					((FussyProgressMonitor) monitor).sanityCheck();
				return null;
			}

			public boolean wasSuccess(Object[] args, Object result, Object[] oldState) throws Exception {
				IFile file = (IFile) args[0];
				return file.exists();
			}

			public void cleanUp(Object[] args, int count) {
				IFile file = (IFile) args[0];
				refreshFile(file);
			}
		}.performTest(inputs);
	}

	public void testSetContents2() {
		IFile target = projects[0].getFile("file1");
		try {
			target.create(null, false, null);
		} catch (CoreException e) {
			fail("0.0", e);
		}

		String testString = getRandomString();
		try {
			target.setContents(getContents(testString), true, false, getMonitor());
		} catch (CoreException e) {
			fail("1.0", e);
		}

		InputStream content = null;
		try {
			content = target.getContents(false);
			assertTrue("get not equal set", compareContent(content, getContents(testString)));
		} catch (CoreException e) {
			fail("2.0", e);
		} finally {
			try {
				content.close();
			} catch (IOException e) {
				// ignore
			}
		}
	}

	public void testAppendContents2() {
		IFile file = projects[0].getFile("file1");
		ensureDoesNotExistInWorkspace(file);

		// If force=true, IFile is non-local, file exists in local file system:
		// make IFile local, append contents (the thinking being that this file,
		// though marked non-local, was in fact local awaiting discovery; so 
		// force=true says we it is ok to make file local and proceed as per normal)
		try {
			// setup
			file.create(null, false, getMonitor());
			assertTrue("1.0", !file.isLocal(IResource.DEPTH_ZERO));
			assertTrue("1.1", !file.getLocation().toFile().exists());
			ensureExistsInFileSystem(file);
			assertTrue("1.2", !file.isLocal(IResource.DEPTH_ZERO));
		} catch (CoreException e) {
			fail("1.3", e);
		}
		try {
			file.appendContents(getRandomContents(), IResource.FORCE, getMonitor());
		} catch (CoreException e) {
			fail("1.4", e);
		}
		assertTrue("1.5", file.isLocal(IResource.DEPTH_ZERO));
		assertTrue("1.6", file.getLocation().toFile().exists());
		// cleanup
		ensureDoesNotExistInWorkspace(file);

		// If force=true, IFile is non-local, file does not exist in local file system:
		// fail - file not local (this file is not local for real - cannot append
		// something to a file that you don't have)
		try {
			// setup
			file.create(null, false, getMonitor());
			assertTrue("2.0", !file.isLocal(IResource.DEPTH_ZERO));
			assertTrue("2.1", !file.getLocation().toFile().exists());
		} catch (CoreException e) {
			fail("2.2", e);
		}
		try {
			file.appendContents(getRandomContents(), IResource.FORCE, getMonitor());
			fail("2.3");
		} catch (CoreException e) {
			// should fail
		}
		assertTrue("2.4", !file.isLocal(IResource.DEPTH_ZERO));
		// cleanup
		ensureDoesNotExistInWorkspace(file);

		// If force=false, IFile is non-local, file exists in local file system:
		// fail - file not local
		try {
			// setup
			file.create(null, false, getMonitor());
			assertTrue("3.0", !file.isLocal(IResource.DEPTH_ZERO));
			assertTrue("3.1", !file.getLocation().toFile().exists());
			ensureExistsInFileSystem(file);
			assertTrue("3.2", !file.isLocal(IResource.DEPTH_ZERO));
		} catch (CoreException e) {
			fail("3.3", e);
		}
		try {
			file.appendContents(getRandomContents(), IResource.NONE, getMonitor());
			fail("3.4");
		} catch (CoreException e) {
			// should fail
		}
		assertTrue("3.5", !file.isLocal(IResource.DEPTH_ZERO));
		// cleanup
		ensureDoesNotExistInWorkspace(file);

		// If force=false, IFile is non-local, file does not exist in local file system:
		// fail - file not local
		try {
			// setup
			file.create(null, false, getMonitor());
			assertTrue("4.0", !file.isLocal(IResource.DEPTH_ZERO));
			assertTrue("4.1", !file.getLocation().toFile().exists());
		} catch (CoreException e) {
			fail("4.2", e);
		}
		try {
			file.appendContents(getRandomContents(), IResource.NONE, getMonitor());
			fail("4.3");
		} catch (CoreException e) {
			// should fail
		}
		assertTrue("4.4", !file.isLocal(IResource.DEPTH_ZERO));
		// cleanup
		ensureDoesNotExistInWorkspace(file);
	}

	public void testSetGetFolderPersistentProperty() throws Throwable {
		IResource target = getWorkspace().getRoot().getFile(new Path("/Project/File.txt"));
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
		assertTrue("2.0", target.getPersistentProperty(name).equals(value));
		// see what happens if we get a non-existant property
		name = new QualifiedName("itp-test", "testNonProperty");
		assertNull("2.1", target.getPersistentProperty(name));

		//set a persistent property with null qualifier
		name = new QualifiedName(null, "foo");
		try {
			target.setPersistentProperty(name, value);
			fail("3.0");
		} catch (CoreException e) {
			//expect an exception
		}
	}
}