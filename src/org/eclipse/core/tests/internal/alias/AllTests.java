/**********************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.tests.internal.alias;

import junit.framework.*;

/**
 * Class for collecting all test classes that deal with alias support.  An alias
 * is a resource in the workspace that has the same file system location as
 * another resource in the workspace.  When a resource changes in a way that
 * affects the contents on disk, all aliases need to be updated.
 */
public class AllTests extends TestCase {
	public AllTests() {
		super(null);
	}
	public AllTests(String name) {
		super(name);
	}
	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTestSuite(BasicAliasTest.class);
		suite.addTestSuite(SyncAliasTest.class);
		return suite;
	}
}