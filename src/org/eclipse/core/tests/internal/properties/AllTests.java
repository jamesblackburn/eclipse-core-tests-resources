package org.eclipse.core.tests.internal.properties;

import junit.framework.*;

public class AllTests extends TestCase {
public AllTests() {
	super(null);
}
public AllTests(String name) {
	super(name);
}
	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTest(PropertyManagerTest.suite());
		return suite;
	}
}
