/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.internal.watson;

import java.io.*;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.internal.watson.*;
import org.eclipse.core.runtime.IPath;

public class DeltaFlatteningTest extends ElementTreeSerializationTest {
	protected ElementTree fNewTree;
	protected IPath project3, folder5, file4, file5;

	public DeltaFlatteningTest() {
		super(null);
	}

	public DeltaFlatteningTest(String name) {
		super(name);
	}

	/**
	 * Performs the serialization activity for this test
	 */
	public Object doRead(ElementTreeReader reader, DataInputStream input) throws IOException {
		return reader.readDelta(fNewTree, input);
	}

	/**
	 * Runs a test for this class at a certain depth and path
	 */
	public void doTest(IPath path, int depth) {

		/* Get an element tree from somewhere. */
		fTree = TestUtil.createTestElementTree();
		fSubtreePath = path;
		fDepth = depth;
		ElementTree newTree = (ElementTree) doPipeTest();
		TestUtil.assertEqualTrees(this.getClass().toString() + "test0", fTree, newTree, fSubtreePath, fDepth);
	}

	/**
	 * Performs the serialization activity for this test
	 */
	public void doWrite(ElementTreeWriter writer, DataOutputStream output) throws IOException {
		writer.writeDelta(fTree, fNewTree, fSubtreePath, fDepth, output, DefaultElementComparator.getComparator());
	}

	protected void setUp() throws Exception {
		super.setUp();
		fTree = TestUtil.createTestElementTree();
		/**
		 * The following changes will be made to the base tree:
		 *	- add project3
		 *  - add folder5 below project3
		 *  - delete file1
		 *  - change data of folder2
		 *	- add file4 below project2
		 *  - add file5 below folder1
		 *  - delete folder3
		 */

		fNewTree = fTree.newEmptyDelta();

		project3 = solution.append("project3");
		folder5 = project3.append("folder5");
		file4 = project2.append("file4");
		file5 = folder1.append("file5");

		fNewTree.createElement(project3, "project3");
		fNewTree.createElement(folder5, "folder5");
		fNewTree.deleteElement(file1);
		fNewTree.createElement(folder2, "ChangedData");
		fNewTree.createElement(file4, "file4");
		fNewTree.createElement(file5, "file5");
		fNewTree.deleteElement(folder3);
		fNewTree.immutable();

		/* assert the new structure */
		TestUtil.assertHasPaths(fNewTree, new IPath[] {solution, project1, project2, project3, file2, file4, file5, folder1, folder2, folder4, folder5});
		TestUtil.assertNoPaths(fNewTree, new IPath[] {file1, file3, folder3});
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(DeltaFlatteningTest.class);
		return suite;
	}

	/**
	 * Tests the reading and writing of element deltas
	 */
	public void test0() {
		doExhaustiveTests();
	}
}
