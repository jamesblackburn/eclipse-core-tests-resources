package org.eclipse.core.tests.internal.localstore;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.File;
import java.util.Date;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.tests.harness.EclipseWorkspaceTest;
//
public class RefreshLocalPerformanceTest extends EclipseWorkspaceTest {
	/** big site default volume (windows) */
	public static final String bigSiteDevice = "d:";

	/** big site initial location */
	public static final IPath bigSiteLocation = new Path(bigSiteDevice, "/bigsite");

	/** benchmark */
	public Date startDate;
public RefreshLocalPerformanceTest() {
	super();
}
public RefreshLocalPerformanceTest(String name) {
	super(name);
}
protected int countChildren(File root) {
	String[] children = root.list();
	if (children == null)
		return 0;
	int result = 0;
	for (int i = 0; i < children.length; i++) {
		File child = new File(root, children[i]);
		if (child.isDirectory())
			result += countChildren(child);
		result++;
	}
	return result;
}
public String dispTime(long diff) {
	return String.valueOf(diff);
}
public void startClock() {
	startDate = new Date();
}
public long stopClock() {
	Date stopDate = new Date();
	return stopDate.getTime() - startDate.getTime();
}
// this test should not be in AllTests because it is noly a performance test
public static Test suite() {
	TestSuite suite = new TestSuite();
	suite.addTest(new RefreshLocalPerformanceTest("testLocalRefreshPerformance"));
	return suite;
}
/**
 * Defines only a default mapping to a project and refreshs locally.
 */
public void testLocalRefreshPerformance() throws Exception {
	// test if the test can be done in this machine
	if (!bigSiteLocation.toFile().isDirectory())
		return;

	// create common objects
	int n = 10;
	IProject project = getWorkspace().getRoot().getProject("MyTestProject");
	IProjectDescription description = getWorkspace().newProjectDescription(project.getName());
	description.setLocation(bigSiteLocation);

	// performance data
	long averageWithTree = 0;
	long averageWithoutTree = 0;
	long[] withoutTree = new long[n];
	long[] withTree = new long[n];

	// report the number of files to be refreshed
	int numberOfFiles = countChildren(bigSiteLocation.toFile());
	System.out.println("Number of local resources: " + numberOfFiles);

	// test each project
	for (int i = 0; i < n; i++) {
		project.create(description, null);
		project.open(null);

		// refresh local (new project)
		System.out.print("NO TREE: ");
		startClock();
		project.refreshLocal(IResource.DEPTH_INFINITE, null);
		withoutTree[i] = stopClock();
		System.out.println(dispTime(withoutTree[i]));
		// test with existing workspace tree
		System.out.print("TREE: ");
		startClock();
		project.refreshLocal(IResource.DEPTH_INFINITE, null);
		withTree[i] = stopClock();
		System.out.println(dispTime(withTree[i]));

		// calculate average
		averageWithoutTree += withoutTree[i];
		averageWithTree += withTree[i];

		// delete project but leave contents
		project.delete(false, false, null);
	}
	averageWithoutTree /= n;
	averageWithTree /= n;
	System.out.println("Average without tree: " + averageWithoutTree);
	System.out.println("Average with tree: " + averageWithTree);
}
}
