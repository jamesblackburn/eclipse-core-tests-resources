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

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.tests.harness.EclipseWorkspaceTest;

/**
 * Tests out of sync cases and refreshLocal in the presence of duplicate
 * resources.
 */
public class SyncAliasTest extends EclipseWorkspaceTest {
	public static Test suite() {
		return new TestSuite(SyncAliasTest.class);
	}
	public SyncAliasTest() {
		super();
	}
	public SyncAliasTest(String name) {
		super(name);
	}
	public void testSimple() {
	}
}
