package org.eclipse.core.tests.internal.localstore;

import org.eclipse.core.runtime.*;
import org.eclipse.core.internal.localstore.*;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.internal.utils.UniversalUniqueIdentifier;
import java.io.*;
import junit.framework.*;
//
public class BlobStoreTest extends LocalStoreTest {
public BlobStoreTest() {
	super();
}
public BlobStoreTest(String name) {
	super(name);
}
public static Test suite() {
	return new TestSuite(BlobStoreTest.class);
}
public void testConstructor() {
	/* build scenario */
	IPath path = getWorkspace().getRoot().getLocation().append("blobstore");
	File temp = path.toFile();
	temp.mkdirs();
	assertTrue("0.1", temp.isDirectory());

	/* null location */
	boolean ok = false;
	try {
		new BlobStore(null, 0);
	} catch (RuntimeException e) {
		ok = true;
	}
	assertTrue("1.1", ok);

	/* empty location */
	ok = false;
	try {
		new BlobStore(Path.EMPTY, 128);
	} catch (RuntimeException e) {
		ok = true;
	}
	assertTrue("2.1", ok);

	/* inexisting location */
	ok = false;
	try {
		new BlobStore(new Path("../this/path/should/not/be/a/folder"), 128);
	} catch (RuntimeException e) {
		ok = true;
	}
	assertTrue("3.1", ok);

	/* invalid limit values */
	ok = false;
	try {
		new BlobStore(path, 0);
	} catch (RuntimeException e) {
		ok = true;
	}
	assertTrue("4.1", ok);
	ok = false;
	try {
		new BlobStore(path, -1);
	} catch (RuntimeException e) {
		ok = true;
	}
	assertTrue("4.2", ok);
	ok = false;
	try {
		new BlobStore(path, 35);
	} catch (RuntimeException e) {
		ok = true;
	}
	assertTrue("4.3", ok);
	ok = false;
	try {
		new BlobStore(path, 512);
	} catch (RuntimeException e) {
		ok = true;
	}
	assertTrue("4.4", ok);
}
public void testDeleteBlob() {
	/* initialize common objects */
	IPath path = getWorkspace().getRoot().getLocation().append("blobstore");
	File root = path.toFile();
	root.mkdirs();
	assertTrue("1.1", root.isDirectory());
	BlobStore store = new BlobStore(path, 64);

	/* delete blob that does not exist */
	assertTrue("2.1", !store.deleteBlob(new UniversalUniqueIdentifier()));

	/* delete existing blob */
	File target = new File(root, "target");
	UniversalUniqueIdentifier uuid = null;
	try {
		createFile(target, "bla bla bla");
		uuid = store.addBlob(target, true);
	} catch (IOException e) {
		fail("4.0", e);
	} catch (CoreException e) {
		fail("4.1", e);
	}
	assertTrue("4.3", store.deleteBlob(uuid));

	/* remove trash */
	Workspace.clear(root);
}
/**
 * #deleteEmptyDir is tested indirectly here.
 */
public void testDeleteEmptyDir() {
	/* initialize common objects */
	IPath path = getWorkspace().getRoot().getLocation().append("blobstore");
	File root = path.toFile();
	root.mkdirs();
	assertTrue("1.1", root.isDirectory());
	// use just one directory
	BlobStore store = new BlobStore(path, 1);

	/* create 2 blobs */
	File target = new File(root, "target");
	File target2 = new File(root, "target2");
	UniversalUniqueIdentifier firstUUID = null;
	UniversalUniqueIdentifier secondUUID = null;
	try {
		createFile(target, "bla bla bla");
		createFile(target2, "bla bla bla");
		firstUUID = store.addBlob(target, true);
		secondUUID = store.addBlob(target2, true);
	} catch (IOException e) {
		fail("4.0", e);
	} catch (CoreException e) {
		fail("4.1", e);
	}

	/* get directory name */
	String[] list = root.list();
	assertTrue("3.0", list != null);
	assertTrue("3.1", list.length == 1);
	File directory = new File(root, list[0]);
	assertTrue("3.2", directory.isDirectory());

	/* delete one, the directory should not be deleted */
	assertTrue("4.1", store.deleteBlob(firstUUID));
	assertTrue("4.2", directory.exists());

	/* delete the other, the directory should be deleted */
	assertTrue("5.1", store.deleteBlob(secondUUID));
	assertTrue("5.2", !directory.exists());

	/* remove trash */
	Workspace.clear(root);
}
public void testGetBlob() {
	/* initialize common objects */
	IPath path = getWorkspace().getRoot().getLocation().append("blobstore");
	File root = path.toFile();
	root.mkdirs();
	assertTrue("1.1", root.isDirectory());
	BlobStore store = new BlobStore(path, 64);

	/* null UUID */
	boolean ok = false;
	try {
		store.getBlob(null);
	} catch (RuntimeException e) {
		ok = true;
	} catch (CoreException e) {
		fail("2.0", e);
	}
	assertTrue("2.1", ok);

	/* get existing blob */
	File target = new File(root, "target");
	UniversalUniqueIdentifier uuid = null;
	String content = "nothing important........tnatropmi gnihton";
	try {
		createFile(target, content);
		uuid = store.addBlob(target, true);
	} catch (IOException e) {
		fail("3.0", e);
	} catch (CoreException e) {
		fail("3.1", e);
	}
	InputStream input = null;
	try {
		input = store.getBlob(uuid);
	} catch (CoreException e) {
		fail("3.4", e);
	}
	assertTrue("4.1", compareContent(getContents(content), input));

	/* remove trash */
	Workspace.clear(root);
}
public void testSetBlob() {
	/* initialize common objects */
	IPath path = getWorkspace().getRoot().getLocation().append("blobstore");
	File root = path.toFile();
	root.mkdirs();
	assertTrue("1.1", root.isDirectory());
	BlobStore store = new BlobStore(path, 64);

	/* normal conditions */
	File target = new File(root, "target");
	UniversalUniqueIdentifier uuid = null;
	String content = "nothing important........tnatropmi gnihton";
	try {
		createFile(target, content);
		uuid = store.addBlob(target, true);
	} catch (IOException e) {
		fail("2.0", e);
	} catch (CoreException e) {
		fail("2.1", e);
	}
	InputStream input = null;
	try {
		input = store.getBlob(uuid);
	} catch (CoreException e) {
		fail("2.4", e);
	}
	assertTrue("2.5", compareContent(getContents(content), input));

	/* remove trash */
	Workspace.clear(root);
}
}
