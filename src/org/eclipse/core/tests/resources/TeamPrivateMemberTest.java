package org.eclipse.core.tests.resources;
/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */

import java.util.HashSet;
import java.util.Set;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.tests.harness.EclipseWorkspaceTest;

public class TeamPrivateMemberTest extends EclipseWorkspaceTest {
public TeamPrivateMemberTest() {
}
public TeamPrivateMemberTest(String name) {
	super(name);
}
public static Test suite() {

	return new TestSuite(TeamPrivateMemberTest.class);

//	TestSuite suite = new TestSuite();
//	suite.addTest(new TeamPrivateMemberTest("testCopy"));
//	return suite;
}

/**
 * Resources which are marked as team private members should always be found.
 */
public void testFindMember() {
	IWorkspaceRoot root = getWorkspace().getRoot();
	IProject project = root.getProject("MyProject");
	IFolder folder = project.getFolder("folder");
	IFile file = project.getFile("file.txt");
	IFile subFile = folder.getFile("subfile.txt");
	IResource[] resources = new IResource[] {project, folder, file, subFile};
	IResource[] members = null;
	ensureExistsInWorkspace(resources, true);

	// no team private members
	assertEquals("1.0", project, root.findMember(project.getFullPath()));
	assertEquals("1.1", folder, root.findMember(folder.getFullPath()));
	assertEquals("1.2", file, root.findMember(file.getFullPath()));
	assertEquals("1.3", subFile, root.findMember(subFile.getFullPath()));
	
	// the folder is team private
	setTeamPrivateMember("2.0", folder, true, IResource.DEPTH_ZERO);
	assertEquals("2.1", project, root.findMember(project.getFullPath()));
	assertEquals("2.2", folder, root.findMember(folder.getFullPath()));
	assertEquals("2.3", file, root.findMember(file.getFullPath()));
	assertEquals("2.4", subFile, root.findMember(subFile.getFullPath()));
	
	// all are team private
	setTeamPrivateMember("3.0", project, true, IResource.DEPTH_INFINITE);
	assertEquals("3.1", project, root.findMember(project.getFullPath()));
	assertEquals("3.2", folder, root.findMember(folder.getFullPath()));
	assertEquals("3.3", file, root.findMember(file.getFullPath()));
	assertEquals("3.4", subFile, root.findMember(subFile.getFullPath()));
}
/**
 * Resources which are marked as team private members are not included in #members
 * calls unless specifically included by calling #members(IContainer.INCLUDE_TEAM_PRIVATE_MEMBERS)
 */
public void testMembers() {
	IProject project = getWorkspace().getRoot().getProject("MyProject");
	IFolder folder = project.getFolder("folder");
	IFile file = project.getFile("file.txt");
	IFile subFile = folder.getFile("subfile.txt");
	IResource[] resources = new IResource[] {project, folder, file, subFile};
	IResource[] members = null;
	ensureExistsInWorkspace(resources, true);
	
	// Initial values should be false.
	assertTeamPrivateMember("1.0", project, false, IResource.DEPTH_INFINITE);

	// Check the calls to #members
	try {
		members = project.members();
	} catch(CoreException e) {
		fail("2.0", e);
	}
	// +1 for the project description file
	assertEquals("2.1", 3, members.length);
	try {
		members = folder.members();
	} catch(CoreException e) {
	}
	assertEquals("2.3", 1, members.length);
	
	// Set the values.
	setTeamPrivateMember("3.0", project, true, IResource.DEPTH_INFINITE);
	assertTeamPrivateMember("3.1", project, true, IResource.DEPTH_INFINITE);
	
	// Check the values
	assertTeamPrivateMember("4.0", project, true, IResource.DEPTH_INFINITE);

	// Check the calls to #members
	try {
		members = project.members();
	} catch(CoreException e) {
		fail("5.0", e);
	}
	assertEquals("5.1", 0, members.length);
	try {
		members = folder.members();
	} catch(CoreException e) {
		fail("5.2", e);
	}
	assertEquals("5.3", 0, members.length);
	
	// FIXME: add the tests for #members(int)
	
	// reset to false
	setTeamPrivateMember("6.0", project, false, IResource.DEPTH_INFINITE);
	assertTeamPrivateMember("6.1", project, false, IResource.DEPTH_INFINITE);
	
	// Check the calls to members(IResource.NONE);
	try {
		members = project.members(IResource.NONE);
	} catch(CoreException e) {
		fail("7.0", e);
	}
	// +1 for the project description file
	assertEquals("7.1", 3, members.length);
	try {
		members = project.members(IContainer.INCLUDE_TEAM_PRIVATE_MEMBERS);
	} catch(CoreException e) {
		fail("7.2", e);
	}
	// +1 for the project description file
	assertEquals("7.3", 3, members.length);
	try {
		members = folder.members();
	} catch (CoreException e) {
		fail("7.4", e);
	}
	assertEquals("7.5", 1, members.length);
	
	// Set one of the children to be TEAM_PRIVATE and try again
	setTeamPrivateMember("8.0", folder, true, IResource.DEPTH_ZERO);
	try {
		members = project.members();
	} catch(CoreException e) {
		fail("8.1", e);
	}
	// +1 for project description, -1 for team private folder
	assertEquals("8.2", 2, members.length);
	try {
		members = folder.members();
	} catch(CoreException e) {
		fail("8.3", e);
	}
	assertEquals("8.4", 1, members.length);
	try {
		members = project.members(IResource.NONE);
	} catch(CoreException e) {
		fail("8.5", e);
	}
	// +1 for project description, -1 for team private folder
	assertEquals("8.6", 2, members.length);
	try {
		members = folder.members();
	} catch(CoreException e) {
		fail("8.7", e);
	}
	assertEquals("8.8", 1, members.length);
	try {
		members = project.members(IContainer.INCLUDE_TEAM_PRIVATE_MEMBERS);
	} catch(CoreException e) {
		fail("8.9", e);
	}
	// +1 for project description
	assertEquals("8.10", 3, members.length);
	try {
		members = folder.members();
	} catch(CoreException e) {
		fail("8.11", e);
	}
	assertEquals("8.12", 1, members.length);
	
	// Set all the resources to be team private
	setTeamPrivateMember("9.0", project, true, IResource.DEPTH_INFINITE);
	assertTeamPrivateMember("9.1", project, true, IResource.DEPTH_INFINITE);
	try {
		members = project.members(IResource.NONE);
	} catch(CoreException e) {
		fail("9.2", e);
	}
	assertEquals("9.3", 0, members.length);
	try {
		members = folder.members(IResource.NONE);
	} catch(CoreException e) {
		fail("9.4", e);
	}
	assertEquals("9.5", 0, members.length);
	try {
		members = project.members(IContainer.INCLUDE_TEAM_PRIVATE_MEMBERS);
	} catch(CoreException e) {
		fail("9.6", e);
	}
	// +1 for project description
	assertEquals("9.7", 3, members.length);
	try {
		members = folder.members(IContainer.INCLUDE_TEAM_PRIVATE_MEMBERS);
	} catch(CoreException e) {
		fail("9.8", e);
	}
	assertEquals("9.9", 1, members.length);
}
/**
 * Test to ensure that team private values are persisted across project close/open.
 */
public void testProjectCloseOpen() {
	// FIXME:
}
/**
 * Resources which are marked as team private members should not be visited by
 * resource visitors.
 */
public void testAccept() {
	IProject project = getWorkspace().getRoot().getProject("MyProject");
	IFolder folder = project.getFolder("folder");
	IFile file = project.getFile("file.txt");
	IFile subFile = folder.getFile("subfile.txt");
	IResource[] resources = new IResource[] {project, folder, file, subFile};
	ensureExistsInWorkspace(resources, true);
	IResource description = project.getFile(IProjectDescription.DESCRIPTION_FILE_NAME);
	Set expected = new HashSet();
	final Set actual = new HashSet();
	
	// default case, no team private members
	for (int i=0; i<resources.length; i++)
		expected.add(resources[i].getFullPath());
	expected.add(description.getFullPath());
	IResourceVisitor visitor = new IResourceVisitor() {
		public boolean visit(IResource resource) {
			actual.add(resource.getFullPath());
			return true;
		}
	};
	try {
		project.accept(visitor);
	} catch(CoreException e) {
		fail("1.0", e);
	}
	assertEquals("1.1", expected, actual);
	actual.clear();
	try {
		project.accept(visitor, IResource.DEPTH_INFINITE, IResource.NONE);
	} catch(CoreException e) {
		fail("1.2", e);
	}
	assertEquals("1.3", expected, actual);
	try {
		project.accept(visitor, IResource.DEPTH_INFINITE, IContainer.INCLUDE_TEAM_PRIVATE_MEMBERS);
	} catch(CoreException e) {
		fail("1.4", e);
	}
	assertEquals("1.5", expected, actual);
	
	// set the folder to be team private. It and its children should
	// be ignored by the visitor
	setTeamPrivateMember("2.0", folder, true, IResource.DEPTH_ZERO);
	expected.clear();
	expected.add(project.getFullPath());
	expected.add(file.getFullPath());
	expected.add(description.getFullPath());
	actual.clear();
	try {
		project.accept(visitor);
	} catch(CoreException e) {
		fail("2.1", e);
	}
	assertEquals("2.2", expected, actual);
	actual.clear();
	try {
		project.accept(visitor, IResource.DEPTH_INFINITE, IResource.NONE);
	} catch(CoreException e) {
		fail("2.3", e);
	}
	assertEquals("2.4", expected, actual);
	// should see all resources if we include the flag
	expected.clear();
	for (int i=0; i<resources.length; i++)
		expected.add(resources[i].getFullPath());
	expected.add(description.getFullPath());
	actual.clear();
	try {
		project.accept(visitor, IResource.DEPTH_INFINITE, IContainer.INCLUDE_TEAM_PRIVATE_MEMBERS);
	} catch(CoreException e) {
		fail("2.5", e);
	}
	assertEquals("2.6", expected, actual);
	// should visit the folder and its members if we call accept on it directly
	expected.clear();
	expected.add(folder.getFullPath());
	expected.add(subFile.getFullPath());
	actual.clear();
	try {
		folder.accept(visitor);
	} catch(CoreException e) {
		fail("2.7", e);
	}
	assertEquals("2.8", expected, actual);
	actual.clear();
	try {
		folder.accept(visitor, IResource.DEPTH_INFINITE, IResource.NONE);
	} catch(CoreException e) {
		fail("2.9", e);
	}
	assertEquals("2.10", expected, actual);
	actual.clear();
	try {
		folder.accept(visitor, IResource.DEPTH_INFINITE, IContainer.INCLUDE_TEAM_PRIVATE_MEMBERS);
	} catch(CoreException e) {
		fail("2.11", e);
	}
	assertEquals("2.11", expected, actual);
	
	// now set all resources to be team private.
	setTeamPrivateMember("3.0", project, true, IResource.DEPTH_INFINITE);
	assertTeamPrivateMember("3.1", project, true, IResource.DEPTH_INFINITE);
	expected.clear();
	expected.add(project.getFullPath());
	actual.clear();
	try {
		project.accept(visitor);
	} catch(CoreException e) {
		fail("3.2", e);
	}
	assertEquals("3.3", expected, actual);
	actual.clear();
	try {
		project.accept(visitor, IResource.DEPTH_INFINITE, IResource.NONE);
	} catch(CoreException e) {
		fail("3.4", e);
	}
	assertEquals("3.5", expected, actual);
	// should see all resources if we include the flag
	expected.clear();
	for (int i=0; i<resources.length; i++)
		expected.add(resources[i].getFullPath());
	expected.add(description.getFullPath());
	actual.clear();
	try {
		project.accept(visitor, IResource.DEPTH_INFINITE, IContainer.INCLUDE_TEAM_PRIVATE_MEMBERS);
	} catch(CoreException e) {
		fail("3.6", e);
	}
	assertEquals("3.7", expected, actual);
}
public void testCopy() {
	IWorkspaceRoot root = getWorkspace().getRoot();
	IProject project = root.getProject("MyProject");
	IFolder folder = project.getFolder("folder");
	IFile file = project.getFile("file.txt");
	IFile subFile = folder.getFile("subfile.txt");
	IResource[] resources = new IResource[] {project, folder, file, subFile};
	ensureExistsInWorkspace(resources, true);
	
	// handles to the destination resources
	IProject destProject = root.getProject("MyOtherProject");
	IFolder destFolder = destProject.getFolder(folder.getName());
	IFile destFile = destProject.getFile(file.getName());
	IFile destSubFile = destFolder.getFile(subFile.getName());
	IResource[] destResources = new IResource[] {destProject, destFolder, destFile, destSubFile};
	ensureDoesNotExistInWorkspace(destResources);

	// set a folder to be team private
	setTeamPrivateMember("1.0", folder, true, IResource.DEPTH_ZERO);
	// copy the project
	int flags = IResource.FORCE;
	try {
		project.copy(destProject.getFullPath(), flags, getMonitor());
	} catch(CoreException e) {
		fail("1.1", e);
	}
	assertExistsInWorkspace("1.2", resources);
	assertExistsInWorkspace("1.3", destResources);
	
	// Do it again and but just copy the folder
	ensureDoesNotExistInWorkspace(destResources);
	ensureExistsInWorkspace(resources, true);
	ensureExistsInWorkspace(destProject, true);
	setTeamPrivateMember("2.0", folder, true, IResource.DEPTH_ZERO);
	try {
		folder.copy(destFolder.getFullPath(), flags, getMonitor());
	} catch(CoreException e) {
		fail("2.1", e);
	}
	assertExistsInWorkspace("2.2", new IResource[] {folder, subFile});
	assertExistsInWorkspace("2.3", new IResource[] {destFolder, destSubFile});
	
	// set all the resources to be team private
	// copy the project
	ensureDoesNotExistInWorkspace(destResources);
	ensureExistsInWorkspace(resources, true);
	setTeamPrivateMember("3.0", project, true, IResource.DEPTH_INFINITE);
	try {
		project.copy(destProject.getFullPath(), flags, getMonitor());
	} catch(CoreException e) {
		fail("3.1", e);
	}
	assertExistsInWorkspace("3.2", resources);
	assertExistsInWorkspace("3.3", destResources);
	
	// do it again but only copy the folder
	ensureDoesNotExistInWorkspace(destResources);
	ensureExistsInWorkspace(resources, true);
	ensureExistsInWorkspace(destProject, true);
	setTeamPrivateMember("4.0", project, true, IResource.DEPTH_INFINITE);
	try {
		folder.copy(destFolder.getFullPath(), flags, getMonitor());
	} catch(CoreException e) {
		fail("4.1", e);
	}
	assertExistsInWorkspace("4.2", new IResource[] {folder, subFile});
	assertExistsInWorkspace("4.3", new IResource[] {destFolder, destSubFile});
}
public void testMove() {
	IWorkspaceRoot root = getWorkspace().getRoot();
	IProject project = root.getProject("MyProject");
	IFolder folder = project.getFolder("folder");
	IFile file = project.getFile("file.txt");
	IFile subFile = folder.getFile("subfile.txt");
	IResource[] resources = new IResource[] {project, folder, file, subFile};
	ensureExistsInWorkspace(resources, true);
	
	// handles to the destination resources
	IProject destProject = root.getProject("MyOtherProject");
	IFolder destFolder = destProject.getFolder(folder.getName());
	IFile destFile = destProject.getFile(file.getName());
	IFile destSubFile = destFolder.getFile(subFile.getName());
	IResource[] destResources = new IResource[] {destProject, destFolder, destFile, destSubFile};
	ensureDoesNotExistInWorkspace(destResources);

	// set a folder to be team private
	setTeamPrivateMember("1.0", folder, true, IResource.DEPTH_ZERO);
	// move the project
	int flags = IResource.FORCE;
	try {
		project.move(destProject.getFullPath(), flags, getMonitor());
	} catch(CoreException e) {
		fail("1.1", e);
	}
	assertDoesNotExistInWorkspace("1.2", resources);
	assertExistsInWorkspace("1.3", destResources);
	
	// Do it again and but just move the folder
	ensureDoesNotExistInWorkspace(destResources);
	ensureExistsInWorkspace(resources, true);
	ensureExistsInWorkspace(destProject, true);
	setTeamPrivateMember("2.0", folder, true, IResource.DEPTH_ZERO);
	try {
		folder.move(destFolder.getFullPath(), flags, getMonitor());
	} catch(CoreException e) {
		fail("2.1", e);
	}
	assertDoesNotExistInWorkspace("2.2", new IResource[] {folder, subFile});
	assertExistsInWorkspace("2.3", new IResource[] {destFolder, destSubFile});
	
	// set all the resources to be team private
	// move the project
	ensureDoesNotExistInWorkspace(destResources);
	ensureExistsInWorkspace(resources, true);
	setTeamPrivateMember("3.0", project, true, IResource.DEPTH_INFINITE);
	try {
		project.move(destProject.getFullPath(), flags, getMonitor());
	} catch(CoreException e) {
		fail("3.1", e);
	}
	assertDoesNotExistInWorkspace("3.2", resources);
	assertExistsInWorkspace("3.3", destResources);
	
	// do it again but only move the folder
	ensureDoesNotExistInWorkspace(destResources);
	ensureExistsInWorkspace(resources, true);
	ensureExistsInWorkspace(destProject, true);
	setTeamPrivateMember("4.0", project, true, IResource.DEPTH_INFINITE);
	try {
		folder.move(destFolder.getFullPath(), flags, getMonitor());
	} catch(CoreException e) {
		fail("4.1", e);
	}
	assertDoesNotExistInWorkspace("4.2", new IResource[] {folder, subFile});
	assertExistsInWorkspace("4.3", new IResource[] {destFolder, destSubFile});
}
public void testDelete() {
	IWorkspaceRoot root = getWorkspace().getRoot();
	IProject project = root.getProject("MyProject");
	IFolder folder = project.getFolder("folder");
	IFile file = project.getFile("file.txt");
	IFile subFile = folder.getFile("subfile.txt");
	IResource[] resources = new IResource[] {project, folder, file, subFile};
	ensureExistsInWorkspace(resources, true);
	
	// default behaviour with no team private members
	int flags = IResource.ALWAYS_DELETE_PROJECT_CONTENT | IResource.FORCE;
	// delete the project
	try {
		project.delete(flags, getMonitor());
	} catch (CoreException e) {
		fail("1.0", e);
	}
	assertDoesNotExistInWorkspace("1.1", resources);
	// delete a file
	ensureExistsInWorkspace(resources, true);
	try {
		file.delete(flags, getMonitor());
	} catch (CoreException e) {
		fail("1.2", e);
	}
	assertDoesNotExistInWorkspace("1.3", file);
	assertExistsInWorkspace("1.4", new IResource[] {project, folder, subFile});
	// delete a folder
	ensureExistsInWorkspace(resources, true);
	try {
		folder.delete(flags, getMonitor());
	} catch(CoreException e) {
		fail("1.5", e);
	}
	assertDoesNotExistInWorkspace("1.6", new IResource[] {folder, subFile});
	assertExistsInWorkspace("1.7", new IResource[] {project, file});
	
	// set one child to be team private
	ensureExistsInWorkspace(resources, true);
	setTeamPrivateMember("2.0", folder, true, IResource.DEPTH_ZERO);
	// delete the project
	try {
		project.delete(flags, getMonitor());
	} catch (CoreException e) {
		fail("2.1", e);
	}
	assertDoesNotExistInWorkspace("2.2", resources);
	// delete a folder
	ensureExistsInWorkspace(resources, true);
	setTeamPrivateMember("2.3", folder, true, IResource.DEPTH_ZERO);
	try {
		folder.delete(flags, getMonitor());
	} catch(CoreException e) {
		fail("2.4", e);
	}
	assertDoesNotExistInWorkspace("2.5", new IResource[] {folder, subFile});
	assertExistsInWorkspace("2.6", new IResource[] {project, file});

	// set all resources to be team private	
	ensureExistsInWorkspace(resources, true);
	setTeamPrivateMember("3.0", project, true, IResource.DEPTH_INFINITE);
	// delete the project
	try {
		project.delete(flags, getMonitor());
	} catch (CoreException e) {
		fail("3.1", e);
	}
	assertDoesNotExistInWorkspace("3.2", resources);
	// delete a file
	ensureExistsInWorkspace(resources, true);
	setTeamPrivateMember("3.3", project, true, IResource.DEPTH_INFINITE);
	try {
		file.delete(flags, getMonitor());
	} catch (CoreException e) {
		fail("3.4", e);
	}
	assertDoesNotExistInWorkspace("3.5", file);
	assertExistsInWorkspace("3.6", new IResource[] {project, folder, subFile});
	// delete a folder
	ensureExistsInWorkspace(resources, true);
	setTeamPrivateMember("3.7", project, true, IResource.DEPTH_INFINITE);
	try {
		folder.delete(flags, getMonitor());
	} catch(CoreException e) {
		fail("3.8", e);
	}
	assertDoesNotExistInWorkspace("3.9", new IResource[] {folder, subFile});
	assertExistsInWorkspace("3.10", new IResource[] {project, file});
}

public void testDeltaAccept() {
	final IWorkspaceRoot root = getWorkspace().getRoot();
	final IProject project = root.getProject("MyProject");
	final IFolder folder = project.getFolder("folder");
	final IFile file = project.getFile("file.txt");
	final IFile subFile = folder.getFile("subfile.txt");
	final IFile description = project.getFile(IProjectDescription.DESCRIPTION_FILE_NAME);
	final IResource[] resources = new IResource[] {project, folder, file, subFile};
	final Set expected = new HashSet();
	
	final IResourceDeltaVisitor visitor = new IResourceDeltaVisitor() {
		public boolean visit(IResourceDelta delta) throws CoreException {
			assertTrue("1.0." + delta.getResource().getFullPath(), expected.contains(delta.getResource()));
			return true;
		}
	};

	IResourceChangeListener listener = new IResourceChangeListener() {
		public void resourceChanged(IResourceChangeEvent event) {
			IResourceDelta delta = event.getDelta();
			assertNotNull("2.0", delta);
			try {
				delta.accept(visitor);
			} catch(CoreException e) {
				fail("2.1", e);
			}
		}
	};
	getWorkspace().addResourceChangeListener(listener);
	try {
		IWorkspaceRunnable body = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				expected.add(root);
				expected.add(project);
				expected.add(folder);
				expected.add(file);
				expected.add(subFile);
				ensureExistsInWorkspace(resources, true);
				expected.add(description);
			}
		};
		try {
			getWorkspace().run(body, getMonitor());
		} catch(CoreException e) {
			fail("3.0", e);
		}
	} finally {
		getWorkspace().removeResourceChangeListener(listener);
	}

	// set the folder to be team private and do the same test
	getWorkspace().addResourceChangeListener(listener);
	try {
		IWorkspaceRunnable body = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				expected.clear();
				expected.add(root);
				expected.add(project);
				expected.add(file);
				ensureExistsInWorkspace(resources, true);
				expected.add(description);
				setTeamPrivateMember("4.0", folder, true, IResource.DEPTH_ZERO);
			}
		};
		try {
			getWorkspace().run(body, getMonitor());
		} catch(CoreException e) {
			fail("4.1", e);
		}
	} finally {
		getWorkspace().removeResourceChangeListener(listener);
	}

	// set all resources to be team private and do the same test
	getWorkspace().addResourceChangeListener(listener);
	try {
		IWorkspaceRunnable body = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				expected.clear();
				expected.add(root);
				expected.add(project);
				ensureExistsInWorkspace(resources, true);
				setTeamPrivateMember("4.0", project, true, IResource.DEPTH_INFINITE);
			}
		};
		try {
			getWorkspace().run(body, getMonitor());
		} catch(CoreException e) {
			fail("4.1", e);
		}
	} finally {
		getWorkspace().removeResourceChangeListener(listener);
	}
}
/**
 * Resources which are marked as team private members return TRUE
 * in all calls to #exists.
 */
public void testExists() {
	IProject project = getWorkspace().getRoot().getProject("MyProject");
	IFolder folder = project.getFolder("folder");
	IFile file = project.getFile("file.txt");
	IFile subFile = folder.getFile("subfile.txt");
	IResource[] resources = new IResource[] {project, folder, file, subFile};
	ensureExistsInWorkspace(resources, true);
	
	// Check to see if all the resources exist in the workspace tree.
	assertExistsInWorkspace("1.0", resources);
	
	// set a folder to be a team private member
	setTeamPrivateMember("2.0", folder, true, IResource.DEPTH_ZERO);
	assertTeamPrivateMember("2.1", folder, true, IResource.DEPTH_ZERO);
	assertExistsInWorkspace("2.2", resources);
	
	// set all resources to be team private
	setTeamPrivateMember("3.0", project, true, IResource.DEPTH_INFINITE);
	assertTeamPrivateMember("3.1", project, true, IResource.DEPTH_INFINITE);
	assertExistsInWorkspace("3.2", resources);
}
/**
 * Test the set and get methods for team private members.
 */
public void testSetGet() {
	IProject project = getWorkspace().getRoot().getProject("MyProject");
	IFolder folder = project.getFolder("folder");
	IFile file = project.getFile("file.txt");
	IFile subFile = folder.getFile("subfile.txt");
	IResource[] resources = new IResource[] {project, folder, file, subFile};
	
	// Trying to set the value on non-existing resources will fail
	for (int i=0; i<resources.length; i++) {
		try {
			resources[i].setTeamPrivateMember(true);
			fail("0.0." + resources[i].getFullPath());
		} catch(CoreException e) {
		}
	}
	
	// create the resources
	ensureExistsInWorkspace(resources, true);
	
	// Initial values should be false.
	for (int i=0; i<resources.length; i++) {
		IResource resource = resources[i];
		assertTrue("1.0: " + resource.getFullPath(), !resource.isTeamPrivateMember());
	}
	
	// Now set the values.
	for (int i = 0; i < resources.length; i++) {
		IResource resource = resources[i];
		try {
			resource.setTeamPrivateMember(true);
		} catch (CoreException e) {
			fail("2.0: " + resource.getFullPath(), e);
		}
	}
	
	// The values should be true for files and folders, false otherwise.
	for (int i = 0; i < resources.length; i++) {
		IResource resource = resources[i];
		switch (resource.getType()) {
			case IResource.FILE:
			case IResource.FOLDER:
				assertTrue("3.0: " + resource.getFullPath(), resource.isTeamPrivateMember());
				break;
			case IResource.PROJECT:
			case IResource.ROOT:
				assertTrue("3.1: " + resource.getFullPath(), !resource.isTeamPrivateMember());
				break;
		}
	}
	
	// Clear the values.
	for (int i = 0; i < resources.length; i++) {
		IResource resource = resources[i];
		try {
			resource.setTeamPrivateMember(false);
		} catch (CoreException e) {
			fail("4.0: " + resource.getFullPath(), e);
		}
	}

	// Values should be false again.
	for (int i=0; i<resources.length; i++) {
		IResource resource = resources[i];
		assertTrue("5.0: " + resource.getFullPath(), !resource.isTeamPrivateMember());
	}
}
protected void assertTeamPrivateMember(final String message, IResource root, final boolean value, int depth) {
	IResourceVisitor visitor = new IResourceVisitor() {
		public boolean visit(IResource resource) throws CoreException {
			boolean expected = false;
			if (resource.getType() == IResource.FILE || resource.getType() == IResource.FOLDER)
				expected = value;
			assertEquals(message + resource.getFullPath(), expected, resource.isTeamPrivateMember());
			return true;
		}
	};
	try {
		root.accept(visitor, depth, IContainer.INCLUDE_TEAM_PRIVATE_MEMBERS);
	} catch(CoreException e) {
		fail(message + "resource.accept", e);
	}
}
protected void setTeamPrivateMember(final String message, IResource root, final boolean value, int depth) {
	IResourceVisitor visitor = new IResourceVisitor() {
		public boolean visit(IResource resource) throws CoreException {
			try {
				resource.setTeamPrivateMember(value);
			} catch(CoreException e) {
				fail(message + resource.getFullPath(), e);
			}
			return true;
		}
	};
	try {
		root.accept(visitor, depth, IContainer.INCLUDE_TEAM_PRIVATE_MEMBERS);
	} catch(CoreException e) {
		fail(message + "resource.accept", e);
	}
}
}
