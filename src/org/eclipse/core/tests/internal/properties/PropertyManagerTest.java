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
package org.eclipse.core.tests.internal.properties;

import java.util.Enumeration;
import java.util.Vector;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.internal.properties.PropertyManager;
import org.eclipse.core.internal.properties.StoredProperty;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.tests.internal.localstore.LocalStoreTest;

public class PropertyManagerTest extends LocalStoreTest {

	public PropertyManagerTest() {
		super(null);
	}

	public PropertyManagerTest(String name) {
		super(name);
	}

	public static Test suite() {
		//	TestSuite suite = new TestSuite();
		//	suite.addTest(new PropertyManagerTest("testConcurrentDelete"));
		//	return suite;
		return new TestSuite(PropertyManagerTest.class);
	}

	/**
	 * Tests concurrent acces to the property store.
	 */
	public void testConcurrentAccess() {

		// create common objects
		final IFile target = projects[0].getFile("target");
		try {
			target.create(getRandomContents(), true, getMonitor());
		} catch (CoreException e) {
			fail("0.0", e);
		}

		// prepare keys and values
		final int N = 50;
		final QualifiedName[] names = new QualifiedName[N];
		final String[] values = new String[N];
		createProperties(target, names, values);

		final CoreException[] errorPointer = new CoreException[1];
		Thread[] threads = createThreads(target, names, values, errorPointer);
		join(threads);
		if (errorPointer[0] != null)
			fail("2.0", errorPointer[0]);

		// remove trash
		try {
			target.delete(true, getMonitor());
		} catch (CoreException e) {
			fail("20.0", e);
		}
	}

	/**
	 * Tests concurrent access to the property store while the project is being
	 * deleted.
	 */
	public void testConcurrentDelete() {
		Thread[] threads;
		final IFile target = projects[0].getFile("target");
		final int REPEAT = 8;
		for (int i = 0; i < REPEAT; i++) {
			// create common objects
			ensureExistsInWorkspace(projects[0], true);
			ensureExistsInWorkspace(target, true);

			// prepare keys and values
			final int N = 50;
			final QualifiedName[] names = new QualifiedName[N];
			final String[] values = new String[N];
			createProperties(target, names, values);

			final CoreException[] errorPointer = new CoreException[1];
			threads = createThreads(target, names, values, errorPointer);
			try {
				//give the threads a chance to start
				Thread.sleep(10);
			} catch (InterruptedException e) {
				fail("1.98", e);
			}
			try {
				//delete the project while the threads are still running
				target.getProject().delete(IResource.NONE, getMonitor());
			} catch (CoreException e) {
				fail("1.99." + i);
			}
			join(threads);
			if (errorPointer[0] != null)
				fail("2.0." + i, errorPointer[0]);
		}
		// remove trash
		try {
			target.delete(true, getMonitor());
		} catch (CoreException e) {
			fail("20.0", e);
		}
	}

	private void join(Thread[] threads) {
		//wait for all threads to finish
		for (int j = 0; j < threads.length; j++) {
			try {
				threads[j].join();
			} catch (InterruptedException e) {
				fail("#join", e);
			}
		}
	}

	private Thread[] createThreads(final IFile target, final QualifiedName[] names, final String[] values, final CoreException[] errorPointer) {
		final int THREAD_COUNT = 3;
		Thread[] threads = new Thread[THREAD_COUNT];
		for (int j = 0; j < THREAD_COUNT; j++) {
			final String id = "GetSetProperty" + j;
			threads[j] = new Thread(new Runnable() {
				public void run() {
					try {
						doGetSetProperties(target, id, names, values);
					} catch (CoreException e) {
						//ignore failure if the project has been deleted
						if (target.exists()) {
							e.printStackTrace();
							errorPointer[0] = e;
							return;
						}
					}
				}
			}, id);
			threads[j].start();
		}
		return threads;
	}

	private void createProperties(IFile target, QualifiedName[] names, String[] values) {
		for (int i = 0; i < names.length; i++) {
			names[i] = new QualifiedName("org.eclipse.core.tests", "prop" + i);
			values[i] = "property value" + i;
		}
		// create properties
		for (int i = 0; i < names.length; i++) {
			try {
				target.setPersistentProperty(names[i], values[i]);
			} catch (CoreException e) {
				fail("1." + i, e);
			}
		}
	}

	protected void doGetSetProperties(IFile target, String threadID, QualifiedName[] names, String[] values) throws CoreException {
		final int N = names.length;
		for (int j = 0; j < 10; j++) {
			for (int i = 0; i < N; i++) {
				target.getPersistentProperty(names[i]);
			}
			// change properties
			for (int i = 0; i < N; i++) {
				//			values[i] = values[i] + " - changed (" + threadID + ")";
				target.setPersistentProperty(names[i], values[i]);
			}
			// verify
			for (int i = 0; i < N; i++) {
				target.getPersistentProperty(names[i]);
			}
		}
	}

	public void testCopy() throws Throwable {
		PropertyManager manager = new PropertyManager((Workspace) getWorkspace());
		IProject source = projects[0];
		IFolder sourceFolder = source.getFolder("myfolder");
		IResource sourceFile = sourceFolder.getFile("myfile.txt");
		IProject destination = projects[1];
		IFolder destFolder = destination.getFolder(sourceFolder.getName());
		IResource destFile = destFolder.getFile(sourceFile.getName());
		QualifiedName propName = new QualifiedName("test", "prop");
		String propValue = "this is the property value";

		/* 
		 * persistent properties 
		 */
		manager.setProperty(source, propName, propValue);
		manager.setProperty(sourceFolder, propName, propValue);
		manager.setProperty(sourceFile, propName, propValue);

		assertNotNull("1.1", manager.getProperty(source, propName));
		assertTrue("1.2", manager.getProperty(source, propName).equals(propValue));
		assertNotNull("1.3", manager.getProperty(sourceFolder, propName));
		assertTrue("1.4", manager.getProperty(sourceFolder, propName).equals(propValue));
		assertNotNull("1.5", manager.getProperty(sourceFile, propName));
		assertTrue("1.6", manager.getProperty(sourceFile, propName).equals(propValue));

		// do the copy at the project level
		manager.copy(source, destination, IResource.DEPTH_INFINITE);

		assertNotNull("1.7", manager.getProperty(destination, propName));
		assertTrue("1.8", manager.getProperty(destination, propName).equals(propValue));

		assertNotNull("1.9", manager.getProperty(destFolder, propName));
		assertTrue("1.10", manager.getProperty(destFolder, propName).equals(propValue));
		assertNotNull("1.11", manager.getProperty(destFile, propName));
		assertTrue("1.12", manager.getProperty(destFile, propName).equals(propValue));

		// do the same thing but copy at the folder level
		manager.deleteProperties(source, IResource.DEPTH_INFINITE);
		manager.deleteProperties(destination, IResource.DEPTH_INFINITE);
		assertNull("2.0", manager.getProperty(source, propName));
		assertNull("2.1", manager.getProperty(sourceFolder, propName));
		assertNull("2.2", manager.getProperty(sourceFile, propName));
		assertNull("2.3", manager.getProperty(destination, propName));
		assertNull("2.4", manager.getProperty(destFolder, propName));
		assertNull("2.5", manager.getProperty(destFile, propName));
		manager.setProperty(sourceFolder, propName, propValue);
		manager.setProperty(sourceFile, propName, propValue);
		assertNotNull("2.6", manager.getProperty(sourceFolder, propName));
		assertTrue("2.7", manager.getProperty(sourceFolder, propName).equals(propValue));
		assertNotNull("2.8", manager.getProperty(sourceFile, propName));
		assertTrue("2.9", manager.getProperty(sourceFile, propName).equals(propValue));

		manager.copy(sourceFolder, destFolder, IResource.DEPTH_INFINITE);

		assertNotNull("2.10", manager.getProperty(destFolder, propName));
		assertTrue("2.11", manager.getProperty(destFolder, propName).equals(propValue));
		assertNotNull("2.12", manager.getProperty(destFile, propName));
		assertTrue("2.13", manager.getProperty(destFile, propName).equals(propValue));

		/* test overwrite */
		String newPropValue = "change property value";
		manager.setProperty(source, propName, newPropValue);
		assertTrue("2.0", manager.getProperty(source, propName).equals(newPropValue));
		manager.copy(source, destination, IResource.DEPTH_INFINITE);
		assertTrue("2.1", manager.getProperty(destination, propName).equals(newPropValue));
	}

	public void testDeleteProperties() throws Throwable {
		/* create common objects */
		PropertyManager manager = new PropertyManager((Workspace) getWorkspace());
		IFile target = projects[0].getFile("target");

		/* server properties */
		QualifiedName propName = new QualifiedName("eclipse", "prop");
		String propValue = "this is the property value";
		manager.setProperty(target, propName, propValue);
		assertTrue("1.1", manager.getProperty(target, propName).equals(propValue));
		/* delete */
		manager.deleteProperties(target, IResource.DEPTH_INFINITE);
		assertTrue("1.3", manager.getProperty(target, propName) == null);

		//test deep deletion of project properties	
		IProject source = projects[0];
		IFolder sourceFolder = source.getFolder("myfolder");
		IResource sourceFile = sourceFolder.getFile("myfile.txt");
		propName = new QualifiedName("test", "prop");
		propValue = "this is the property value";

		/* 
		 * persistent properties 
		 */
		manager.setProperty(source, propName, propValue);
		manager.setProperty(sourceFolder, propName, propValue);
		manager.setProperty(sourceFile, propName, propValue);

		assertNotNull("2.1", manager.getProperty(source, propName));
		assertTrue("2.2", manager.getProperty(source, propName).equals(propValue));
		assertNotNull("2.3", manager.getProperty(sourceFolder, propName));
		assertTrue("2.4", manager.getProperty(sourceFolder, propName).equals(propValue));
		assertNotNull("2.5", manager.getProperty(sourceFile, propName));
		assertTrue("2.6", manager.getProperty(sourceFile, propName).equals(propValue));

		//delete properties
		manager.deleteProperties(source, IResource.DEPTH_INFINITE);
		assertNull("3.1", manager.getProperty(source, propName));
		assertNull("3.2", manager.getProperty(sourceFolder, propName));
		assertNull("3.3", manager.getProperty(sourceFile, propName));

	}

	/**
	 * Do a stress test by adding a very large property to the store.
	 */
	public void testLargeProperty() {
		// create common objects
		IFile target = projects[0].getFile("target");
		try {
			target.create(getRandomContents(), true, getMonitor());
		} catch (CoreException e) {
			fail("0.0", e);
		}

		QualifiedName name = new QualifiedName("stressTest", "prop");
		final int SIZE = 10000;
		StringBuffer valueBuf = new StringBuffer(SIZE);
		for (int i = 0; i < SIZE; i++) {
			valueBuf.append("a");
		}
		String value = valueBuf.toString();
		try {
			target.setPersistentProperty(name, value);
			//should fail
			fail("1.0");
		} catch (CoreException e) {
			// expected
		}

		// remove trash
		try {
			target.delete(true, getMonitor());
		} catch (CoreException e) {
			fail("20.0", e);
		}
	}

	public void testProperties() throws Throwable {
		IProgressMonitor monitor = null;

		// create common objects
		PropertyManager manager = new PropertyManager((Workspace) getWorkspace());
		IFile target = projects[0].getFile("target");
		target.create(null, false, monitor);

		// these are the properties that we are going to use
		QualifiedName propName1 = new QualifiedName("org.eclipse.core.tests", "prop1");
		QualifiedName propName2 = new QualifiedName("org.eclipse.core.tests", "prop2");
		QualifiedName propName3 = new QualifiedName("org.eclipse.core.tests", "prop3");
		String propValue1 = "this is the property value1";
		String propValue2 = "this is the property value2";
		String propValue3 = "this is the property value3";
		Vector props = new Vector(3);
		props.addElement(new StoredProperty(propName1, propValue1));
		props.addElement(new StoredProperty(propName2, propValue2));
		props.addElement(new StoredProperty(propName3, propValue3));

		// set the properties individually and retrieve them
		for (Enumeration e = props.elements(); e.hasMoreElements();) {
			StoredProperty prop = (StoredProperty) e.nextElement();
			manager.setProperty(target, prop.getName(), prop.getStringValue());
			assertTrue("1.0." + prop.getName(), prop.getStringValue().equals(manager.getProperty(target, prop.getName())));
		}
		manager.deleteProperties(target, IResource.DEPTH_INFINITE);

		// remove trash
		target.delete(false, monitor);
	}

	public void testSimpleUpdate() {

		// create common objects
		IFile target = projects[0].getFile("target");
		try {
			target.create(getRandomContents(), true, getMonitor());
		} catch (CoreException e) {
			fail("0.0", e);
		}

		// prepare keys and values
		int N = 3;
		QualifiedName[] names = new QualifiedName[3];
		String[] values = new String[N];
		for (int i = 0; i < N; i++) {
			names[i] = new QualifiedName("org.eclipse.core.tests", "prop" + i);
			values[i] = "property value" + i;
		}

		// create properties
		for (int i = 0; i < N; i++) {
			try {
				target.setPersistentProperty(names[i], values[i]);
			} catch (CoreException e) {
				fail("1." + i, e);
			}
		}

		// verify
		for (int i = 0; i < N; i++) {
			try {
				assertTrue("2.0", target.getPersistentProperty(names[i]).equals(values[i]));
			} catch (CoreException e) {
				fail("3." + i, e);
			}
		}

		for (int j = 0; j < 20; j++) {

			// change properties
			for (int i = 0; i < N; i++) {
				try {
					values[i] = values[i] + " - changed";
					target.setPersistentProperty(names[i], values[i]);
				} catch (CoreException e) {
					fail("4." + i, e);
				}
			}

			// verify
			for (int i = 0; i < N; i++) {
				try {
					assertTrue("5.0", target.getPersistentProperty(names[i]).equals(values[i]));
				} catch (CoreException e) {
					fail("6." + i, e);
				}
			}

		}

		// remove trash
		try {
			target.delete(true, getMonitor());
		} catch (CoreException e) {
			fail("20.0", e);
		}
	}
}