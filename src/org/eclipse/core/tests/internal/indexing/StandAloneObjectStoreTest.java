package org.eclipse.core.tests.internal.indexing;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import junit.textui.TestRunner;

public class StandAloneObjectStoreTest {

	public static void main(String[] args) {
		TestRunner.run(BasicObjectStoreTest.suite(new StandAloneTestEnvironment()));
	}
	
}
