/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
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
import java.util.ArrayList;
import java.util.Properties;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.preferences.*;
import org.eclipse.core.tests.harness.EclipseWorkspaceTest;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * @since 3.0
 */
public class ProjectPreferencesTest extends EclipseWorkspaceTest {

	public static Test suite() {
		// all test methods are named "test..."
		return new TestSuite(ProjectPreferencesTest.class);
		//				TestSuite suite = new TestSuite();
		//				suite.addTest(new ProjectPreferencesTest("test_55410"));
		//				return suite;
	}

	public ProjectPreferencesTest(String name) {
		super(name);
	}

	public void testSimple() {
		IProject project = getWorkspace().getRoot().getProject("foo");
		String qualifier = "org.eclipse.core.tests.resources";
		String key = "key" + getUniqueString();
		String instanceValue = "instance" + getUniqueString();
		String projectValue = "project" + getUniqueString();
		IScopeContext projectContext = new ProjectScope(project);
		IScopeContext instanceContext = new InstanceScope();
		ensureExistsInWorkspace(project, true);

		ArrayList list = new ArrayList();
		list.add(null);
		list.add(new IScopeContext[0]);
		list.add(new IScopeContext[] {null});
		IScopeContext[][] contextsWithoutScope = (IScopeContext[][]) list.toArray(new IScopeContext[list.size()][]);

		list = new ArrayList();
		list.add(new IScopeContext[] {projectContext});
		list.add(new IScopeContext[] {null, projectContext});
		IScopeContext[][] contextsWithScope = (IScopeContext[][]) list.toArray(new IScopeContext[list.size()][]);

		// set a preference value in the instance scope
		IPreferencesService service = Platform.getPreferencesService();
		Preferences node = instanceContext.getNode(qualifier);
		node.put(key, instanceValue);
		String actual = node.get(key, null);
		assertNotNull("1.0", actual);
		assertEquals("1.1", instanceValue, actual);

		// get the value through service searching
		for (int i = 0; i < contextsWithoutScope.length; i++) {
			actual = service.getString(qualifier, key, null, contextsWithoutScope[i]);
			assertNotNull("2.0." + i, actual);
			assertEquals("2.1." + i, instanceValue, actual);
		}
		for (int i = 0; i < contextsWithScope.length; i++) {
			actual = service.getString(qualifier, key, null, contextsWithScope[i]);
			assertNotNull("2.2." + i, actual);
			assertEquals("2.3." + i, instanceValue, actual);
		}

		// set a preference value in the project scope
		node = projectContext.getNode(qualifier);
		node.put(key, projectValue);
		actual = node.get(key, null);
		assertNotNull("3.0", actual);
		assertEquals("3.1", projectValue, actual);

		// get the value through service searching
		for (int i = 0; i < contextsWithoutScope.length; i++) {
			actual = service.getString(qualifier, key, null, contextsWithoutScope[i]);
			assertNotNull("4.0." + i, actual);
			assertEquals("4.1." + i, instanceValue, actual);
		}
		for (int i = 0; i < contextsWithScope.length; i++) {
			actual = service.getString(qualifier, key, null, contextsWithScope[i]);
			assertNotNull("4.2." + i, actual);
			assertEquals("4.3." + i, projectValue, actual);
		}

		// remove the project scope value
		node = projectContext.getNode(qualifier);
		node.remove(key);
		actual = node.get(key, null);
		assertNull("5.0", actual);

		// get the value through service searching
		for (int i = 0; i < contextsWithoutScope.length; i++) {
			actual = service.getString(qualifier, key, null, contextsWithoutScope[i]);
			assertNotNull("6.0." + i, actual);
			assertEquals("6.1." + i, instanceValue, actual);
		}
		for (int i = 0; i < contextsWithScope.length; i++) {
			actual = service.getString(qualifier, key, null, contextsWithScope[i]);
			assertNotNull("6.2." + i, actual);
			assertEquals("6.3." + i, instanceValue, actual);
		}

		// remove the instance value so there is nothing
		node = instanceContext.getNode(qualifier);
		node.remove(key);
		actual = node.get(key, null);
		for (int i = 0; i < contextsWithoutScope.length; i++) {
			actual = service.getString(qualifier, key, null, contextsWithoutScope[i]);
			assertNull("7.0." + i, actual);
		}
		for (int i = 0; i < contextsWithScope.length; i++) {
			actual = service.getString(qualifier, key, null, contextsWithScope[i]);
			assertNull("7.1." + i, actual);
		}
	}

	public void testListener() {
		// setup
		IProject project = getWorkspace().getRoot().getProject(getUniqueString());
		String qualifier = "org.eclipse.core.tests.resources";
		String key = "key" + getUniqueString();
		String value = "value" + getUniqueString();
		IScopeContext projectContext = new ProjectScope(project);
		// create project
		ensureExistsInWorkspace(project, true);
		// set preferences
		Preferences node = projectContext.getNode(qualifier);
		node.put(key, value);
		String actual = node.get(key, null);
		assertNotNull("1.0", actual);
		assertEquals("1.1", value, actual);
		try {
			// flush
			node.flush();
		} catch (BackingStoreException e) {
			fail("0.0", e);
		}

		// get settings filename
		File file = project.getLocation().append(".settings").append(qualifier + ".prefs").toFile();
		Properties props = new Properties();
		InputStream input = null;
		try {
			input = new BufferedInputStream(new FileInputStream(file));
			props.load(input);
		} catch (IOException e) {
			fail("1.0", e);
		} finally {
			if (input != null)
				try {
					input.close();
				} catch (IOException e) {
					// ignore
				}
		}

		// change settings in the file
		String newKey = "newKey" + getUniqueString();
		String newValue = "newValue" + getUniqueString();
		props.put(newKey, newValue);

		// save the file and ensure timestamp is different
		OutputStream output = null;
		try {
			output = new BufferedOutputStream(new FileOutputStream(file));
			props.store(output, null);
		} catch (IOException e) {
			fail("2.0", e);
		} finally {
			if (output != null)
				try {
					output.close();
				} catch (IOException e) {
					// ignore
				}
		}

		IFile workspaceFile = project.getFolder(".settings").getFile(qualifier + ".prefs");

		// ensure that the file is out-of-sync with the workspace
		// by changing the lastModified time
		touchInFilesystem(workspaceFile);

		// resource change is fired
		try {
			workspaceFile.refreshLocal(IResource.DEPTH_ZERO, getMonitor());
		} catch (CoreException e) {
			fail("3.1", e);
		}

		// validate new settings
		actual = node.get(key, null);
		assertEquals("4.1", value, actual);
		actual = node.get(newKey, null);
		assertEquals("4.2", newValue, actual);
	}

	/**
	 * Regression test for bug 60896 - Project preferences remains when deleting/creating project
	 */
	public void testProjectDelete() {
		// create the project
		IProject project = getWorkspace().getRoot().getProject(getUniqueString());
		ensureExistsInWorkspace(project, true);
		// set some settings
		String qualifier = getUniqueString();
		String key = getUniqueString();
		String value = getUniqueString();
		IScopeContext context = new ProjectScope(project);
		Preferences node = context.getNode(qualifier);
		Preferences parent = node.parent().parent();
		node.put(key, value);
		assertEquals("1.0", value, node.get(key, null));

		try {
			// delete the project
			project.delete(IResource.FORCE | IResource.ALWAYS_DELETE_PROJECT_CONTENT, getMonitor());
		} catch (CoreException e) {
			fail("2.0", e);
		}

		try {
			// project pref should not exist
			assertTrue("3.0", !parent.nodeExists(project.getName()));
		} catch (BackingStoreException e) {
			fail("3.1", e);
		}

		// create a project with the same name
		ensureExistsInWorkspace(project, true);

		// ensure that the preference value is not set
		assertNull("4.0", context.getNode(qualifier).get(key, null));
	}

	/**
	 * Regression test for Bug 60925 - project preferences do not show up in workspace.
	 * 
	 * Initially we were using java.io.File APIs and writing the preferences files
	 * directly to disk. We need to convert to use Resource APIs so changes
	 * show up in the workspace immediately.
	 */
	public void test_60925() {
		// setup
		IProject project = getWorkspace().getRoot().getProject(getUniqueString());
		ensureExistsInWorkspace(project, true);
		String qualifier = getUniqueString();
		String dirName = ".settings";
		String fileExtension = "prefs";
		IFile file = project.getFile(new Path(dirName).append(qualifier).addFileExtension(fileExtension));

		// should be nothing in the file system
		assertTrue("0.0", !file.exists());
		assertTrue("0.1", !file.getLocation().toFile().exists());

		// store a preference key/value pair
		IScopeContext context = new ProjectScope(project);
		String key = getUniqueString();
		String value = getUniqueString();
		Preferences node = context.getNode(qualifier);
		node.put(key, value);

		// flush changes to disk
		try {
			node.flush();
		} catch (BackingStoreException e) {
			fail("1.0", e);
		}

		// changes should appear in the workspace
		assertTrue("2.0", file.exists());
		assertTrue("2.1", file.isSynchronized(IResource.DEPTH_ZERO));
	}

	/**
	 * Bug 55410 - [runtime] prefs: keys and valid chars
	 *
	 * Problems with a dot "." as a key name
	 */
	public void test_55410() {
		IWorkspace workspace = getWorkspace();
		IProject project1 = workspace.getRoot().getProject(getUniqueString());
		ensureExistsInWorkspace(new IResource[] {project1}, true);
		Preferences node = new ProjectScope(project1).getNode(ResourcesPlugin.PI_RESOURCES).node("subnode");
		String key1 = ".";
		String key2 = "x";
		String value1 = getUniqueString();
		String value2 = getUniqueString();
		node.put(key1, value1);
		node.put(key2, value2);
		assertEquals("0.8", value1, node.get(key1, null));
		assertEquals("0.9", value2, node.get(key2, null));
		IFile prefsFile = project1.getFolder(".settings").getFile(ResourcesPlugin.PI_RESOURCES + ".prefs");
		assertTrue("1.0", !prefsFile.exists());
		try {
			node.flush();
		} catch (BackingStoreException e) {
			fail("1.1", e);
		}
		assertTrue("1.1", prefsFile.exists());
		Properties props = new Properties();
		InputStream contents = null;
		try {
			contents = prefsFile.getContents();
		} catch (CoreException e) {
			fail("1.2", e);
		}
		try {
			props.load(contents);
		} catch (IOException e) {
			fail("1.3", e);
		} finally {
			if (contents != null)
				try {
					contents.close();
				} catch (IOException e) {
					// ignore
				}
		}
		assertEquals("2.0", value2, props.getProperty("subnode/" + key2));
		assertEquals("2.1", value1, props.getProperty("subnode/" + key1));
	}

	/**
	 * Bug 61277 - preferences and project moves
	 *
	 * Investigate what happens with project preferences when the
	 * project is moved.
	 */
	public void test_61277a() {
		IProject project = getWorkspace().getRoot().getProject(getUniqueString());
		IProject destProject = getWorkspace().getRoot().getProject(getUniqueString());
		ensureExistsInWorkspace(project, true);
		ensureDoesNotExistInWorkspace(destProject);
		IScopeContext context = new ProjectScope(project);
		String qualifier = getUniqueString();
		Preferences node = context.getNode(qualifier);
		String key = getUniqueString();
		String value = getUniqueString();
		node.put(key, value);
		assertEquals("1.0", value, node.get(key, null));

		try {
			// save the prefs
			node.flush();
		} catch (BackingStoreException e) {
			fail("1.1", e);
		}

		// rename the project
		try {
			project.move(destProject.getFullPath(), true, getMonitor());
		} catch (CoreException e) {
			fail("2.0", e);
		}

		context = new ProjectScope(destProject);
		node = context.getNode(qualifier);
		assertEquals("3.0", value, node.get(key, null));
	}

	/**
	 * Bug 61277 - preferences and project moves
	 *
	 * Investigate what happens with project preferences when the
	 * project is moved.
	 */
	public void test_61277b() {
		IWorkspace workspace = getWorkspace();
		IProject project1 = workspace.getRoot().getProject(getUniqueString());
		IProject project2 = workspace.getRoot().getProject(getUniqueString());
		ensureExistsInWorkspace(new IResource[] {project1}, true);
		Preferences node = new ProjectScope(project1).getNode(ResourcesPlugin.PI_RESOURCES);
		node.put("key", "value");
		assertTrue("1.0", !project1.getFolder(".settings").getFile(ResourcesPlugin.PI_RESOURCES + ".prefs").exists());
		try {
			node.flush();
		} catch (BackingStoreException e) {
			fail("1.99", e);
		}
		assertTrue("1.1", project1.getFolder(".settings").getFile(ResourcesPlugin.PI_RESOURCES + ".prefs").exists());
		// move project and ensures charsets settings are preserved
		try {
			project1.move(project2.getFullPath(), false, null);
		} catch (CoreException e) {
			fail("2.99", e);
		}
		assertTrue("2.0", project2.getFolder(".settings").getFile(ResourcesPlugin.PI_RESOURCES + ".prefs").exists());
		node = new ProjectScope(project2).getNode(ResourcesPlugin.PI_RESOURCES);
		assertEquals("2.1", "value", node.get("key", null));
	}

	/**
	 * Bug 61277 - preferences and project moves
	 *
	 * Investigate what happens with project preferences when the
	 * project is moved.
	 * 
	 * Problems with a key which is the empty string.
	 */
	public void test_61277c() {
		IWorkspace workspace = getWorkspace();
		IProject project1 = workspace.getRoot().getProject(getUniqueString());
		IProject project2 = null;
		ensureExistsInWorkspace(new IResource[] {project1}, true);
		Preferences node = new ProjectScope(project1).getNode(ResourcesPlugin.PI_RESOURCES);
		String key1 = "key";
		String emptyKey = "";
		String value1 = getUniqueString();
		String value2 = getUniqueString();
		node.put(key1, value1);
		node.put(emptyKey, value2);
		assertTrue("1.0", !project1.getFolder(".settings").getFile(ResourcesPlugin.PI_RESOURCES + ".prefs").exists());

		try {
			node.flush();
		} catch (BackingStoreException e) {
			fail("1.1", e);
		}
		assertTrue("1.2", project1.getFolder(".settings").getFile(ResourcesPlugin.PI_RESOURCES + ".prefs").exists());

		// move project and ensures charsets settings are preserved
		project2 = workspace.getRoot().getProject(getUniqueString());
		try {
			project1.move(project2.getFullPath(), false, null);
		} catch (CoreException e) {
			fail("2.99", e);
		}
		assertTrue("2.0", project2.getFolder(".settings").getFile(ResourcesPlugin.PI_RESOURCES + ".prefs").exists());

		node = new ProjectScope(project2).getNode(ResourcesPlugin.PI_RESOURCES);
		assertEquals("2.1", value1, node.get(key1, null));
		assertEquals("2.2", value2, node.get(emptyKey, null));
	}

	private void touchInFilesystem(IFile file) {
		for (int count = 0; count < 30 && file.isSynchronized(IResource.DEPTH_ZERO); count++) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// ignore
			}
			file.getLocation().toFile().setLastModified(System.currentTimeMillis());
		}
		assertTrue("File not out of sync: " + file.getLocation().toOSString(), !file.isSynchronized(IResource.DEPTH_ZERO));
	}
}