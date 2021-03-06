/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.resources.perf;

import java.util.Random;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.filesystem.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.tests.harness.FileSystemHelper;
import org.eclipse.core.tests.harness.PerformanceTestRunner;
import org.eclipse.core.tests.internal.localstore.LocalStoreTest;

/**
 * Automated performance tests for file system operations. 
 */
public class FileSystemPerformanceTest extends LocalStoreTest {

	private static final String chars = "abcdefghijklmnopqrstuvwxyz";
	private static final int FILE_COUNT = 100;
	private static final int DIR_COUNT = 25;

	private static final int OUTER = 4;
	private static final int INNER = 1;

	private final Random random = new Random();
	private IFileStore baseStore;

	public String createString(int length) {
		StringBuffer buf = new StringBuffer(length);
		//fill the string with random characters up to the desired length
		for (int i = 0; i < length; i++) {
			buf.append(chars.charAt(random.nextInt(chars.length())));
		}
		return buf.toString();
	}

	void createStructure() throws CoreException {
		baseStore = EFS.getLocalFileSystem().getStore(FileSystemHelper.getRandomLocation(getTempDir()));
		baseStore.mkdir(EFS.NONE, null);
		for (int i = 0; i < DIR_COUNT; i++) {
			IFileStore dir = baseStore.getChild(createString(8));
			dir.mkdir(EFS.SHALLOW, null);
			for (int j = 0; j < FILE_COUNT; j++) {
				IFileStore file = dir.getChild(createString(16));
				createFile(file, createString(16));
			}
		}
	}

	void setAttributesOnTree() throws CoreException {
		IFileStore[] dirs = baseStore.childStores(EFS.NONE, null);
		for (int i = 0; i < dirs.length; i++) {
			IFileStore[] files = dirs[i].childStores(EFS.NONE, null);
			for (int j = 0; j < files.length; j++) {
				IFileInfo fileInfo = files[j].fetchInfo();
				boolean clear = fileInfo.getAttribute(EFS.ATTRIBUTE_READ_ONLY);
				fileInfo.setAttribute(EFS.ATTRIBUTE_EXECUTABLE | EFS.ATTRIBUTE_READ_ONLY, !clear);
				files[j].putInfo(fileInfo, EFS.SET_ATTRIBUTES, null);
			}
		}
	}

	public void testPutFileInfo() throws CoreException {
		createStructure();
		PerformanceTestRunner runner = new PerformanceTestRunner() {
			protected void test() {
				try {
					setAttributesOnTree();
				} catch (Exception e) {
					fail("Failed to set attributes during performance test", e);
				}
			}
		};
		runner.run(this, OUTER, INNER);
		baseStore.delete(EFS.NONE, null);
	}

	public static Test suite() {
		return new TestSuite(FileSystemPerformanceTest.class);
	}
}
