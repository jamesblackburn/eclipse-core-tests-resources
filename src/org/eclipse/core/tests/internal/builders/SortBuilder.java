package org.eclipse.core.tests.internal.builders;

import java.io.*;
import java.util.Map;

import org.eclipse.core.internal.resources.ResourceException;
import org.eclipse.core.resources.*;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.*;

/**
 * A <code>SortBuilder</code> maintains a collection of files located
 * under a project's "sorted" folder.  These files mirror another
 * collection of files located under the project's "unsorted" folder.
 * The content (bytes) of each sorted file is the content of the
 * unsorted file it mirrors, but sorted in either ascending or
 * descending order depending on the build command.
 * @see SortBuilderPlugin
 */
public class SortBuilder extends TestBuilder {
	public static final String BUILDER_NAME = "org.eclipse.core.tests.resources.sortbuilder";
	/**
	 * The singleton builder instance
	 */
	protected static SortBuilder singleton;
	
	/**
	 * Whether the last build was full, auto or incremental
	 */
	private int triggerForLastBuild;

	/**
	 * Build command parameters.
	 */
	public static String SORT_ORDER = "SortOrder";
	public static String ASCENDING = "Ascending";
	public static String DESCENDING = "Descending";
	public static String SORTED_FOLDER = "SortedFolder";
	public static String UNSORTED_FOLDER = "UnsortedFolder";

	/**
	 * Default folders
	 */
	public static String DEFAULT_SORTED_FOLDER = "SortedFolder";
	public static String DEFAULT_UNSORTED_FOLDER = "UnsortedFolder";
public SortBuilder() {
	singleton = this;
}
/**
 * Implemements the inherited abstract method in
 * <code>BaseBuilder</code>.
 * @see BaseBuilder#build(IResourceDelta,int,IProgressMonitor)
 */
protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
	arguments = args;
	super.build(kind, args,  monitor);
	triggerForLastBuild = kind;
	IResourceDelta delta = getDelta(getProject());

	if (delta == null || kind == IncrementalProjectBuilder.FULL_BUILD) {
		fullBuild();
	} else {
		try {
			incrementalBuild(delta);
		} catch (Exception e) {
			throw new CoreException(new Status(IStatus.ERROR, "tests", IResourceStatus.BUILD_FAILED, "Incremental build failed due to internal error", e));
		}
	}
	return new IProject[0];
}
/**
 * Sort the given unsorted file in either ascending or descending
 * order (depending on the build command) and update its corresponding
 * sorted file with the result.
 * @param unsortedFile
 * @exception Exception if the build can't proceed
 */
private void build(IFile unsortedFile) throws CoreException {
	InputStream is = unsortedFile.getContents();
	ByteArrayOutputStream bos = new ByteArrayOutputStream();

	try {
		try {
			int c;
			while((c = is.read()) != -1){
				bos.write(c);
			}
		} finally {
			is.close();
		}
	} catch (IOException e) {
		throw new ResourceException(IResourceStatus.BUILD_FAILED, null, "IOException sorting file", e);
	}

	byte[] bytes = bos.toByteArray();

	quicksort(bytes, 0, bytes.length - 1, isSortOrderAscending());

	IFile sortedFile = (IFile)convertToSortedResource(unsortedFile);
	createResource(sortedFile);

	ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
	sortedFile.setContents(bis, true, false, null);
}
/**
 * Converts the given unsorted resource handle to its corresponding
 * sorted resource handle.  Neither resources need exist, but their
 * types must be a folder (FOLDER) or a file (FILE).
 * @param IResource
 * @exception Exception if the resource is not a folder or a file
 */
private IResource convertToSortedResource(IResource unsortedResource) throws CoreException {
	IPath sortedFolderRelativePath = unsortedResource.getProjectRelativePath().removeFirstSegments(1);

	int type = unsortedResource.getType();

	if (type == IResource.FOLDER) {
		return getSortedFolder().getFolder(sortedFolderRelativePath);
	} else
		if (type == IResource.FILE) {
			return getSortedFolder().getFile(sortedFolderRelativePath);
		} else {
			throw new ResourceException(IResourceStatus.RESOURCE_WRONG_TYPE, null, "Wrong resource type", null);
		}
}
/**
 * Creates the given resource, and its parent if necessary.  The
 * resource must not be a solution and it's project must exist.
 * @param resource
 * @exception Exception if the resource is not a folder or a file
 */
private void createResource(IResource resource) throws CoreException {
	if (resource.exists()) {
		return;
	}

	createResource(resource.getParent());

	int type = resource.getType();

	if (type == IResource.FOLDER) {
		((IFolder) resource).create(true, true, null);
	} else
		if (type == IResource.FILE) {
			((IFile) resource).create(null, true, null);
		} else {
			throw new ResourceException(IResourceStatus.RESOURCE_WRONG_TYPE, null, "Wrong resource type", null);
		}
}
/**
 * Deletes the given resource, and its children if necessary.  The
 * resource must not be a solution and its project must exist.
 * @param resource
 * @exception CoreException if the resource is not a folder or a file
 */
private void deleteResource(IResource resource) throws CoreException {
	if (!resource.exists()) {
		return;
	}

	int type = resource.getType();

	if (type == IResource.FOLDER) {
		IFolder folder = (IFolder) resource;
		IResource[] members = folder.members();
		for (int i = 0; i < members.length; i++) {
			deleteResource((IResource) members[i]);
		}
		folder.delete(true, null);
	} else
		if (type == IResource.FILE) {
			((IFile) resource).delete(true, null);
		} else {
			throw new ResourceException(IResourceStatus.RESOURCE_WRONG_TYPE, null, "Wrong resource type", null);
		}
}
/**
 * Performs a full build by discarding the results of the previous
 * build and sorting all files in the unsorted folder.
 * @exception BuildException if the build can't proceed
 */
private void fullBuild() throws CoreException {
	try {
		IFolder sortedFolder = getSortedFolder();
		IFolder unsortedFolder = getUnsortedFolder();

		if(sortedFolder.exists()){
			//delete all sorted files
			IResource[] members = sortedFolder.members();
			for(int i = 0; i < members.length; i++) {
				deleteResource((IResource)members[i]);
			}
		}

		if(unsortedFolder.exists()){
			fullBuild(unsortedFolder);
		}
	} catch(Exception e){
		throw new ResourceException(IResourceStatus.BUILD_FAILED, null, "Sort builder failed", e);
	}
}
/**
 * Performs a full build on the given unsorted resource by sorting
 * it, if it is a file, and sorting its children, if it is a folder.
 * @exception Exception if the build can't proceed
 */
private void fullBuild(IResource unsortedResource) throws Exception {
	if(unsortedResource.getType() == IResource.FILE){
		build((IFile)unsortedResource);
	} else {
		IResource[] members = ((IFolder)unsortedResource).members();
		for(int i = 0; i < members.length; i++){
			IResource member = (IResource)members[i];
			fullBuild(member);
		}
	}
}
public static SortBuilder getInstance() {
	return singleton;
}
/**
 * Returns the folder under which sorted resources are found.
 * @return IFolder
 */
private IFolder getSortedFolder() {
	String sortedFolder = (String) arguments.get(SORTED_FOLDER);
	if (sortedFolder == null)
		sortedFolder = DEFAULT_SORTED_FOLDER;
	forgetLastBuiltState();
	return getProject().getFolder(sortedFolder);
}
/**
 * Returns the folder under which unsorted resources are found.
 * @return IFolder
 */
private IFolder getUnsortedFolder() {
	String unsortedFolder = (String) arguments.get(UNSORTED_FOLDER);
	if (unsortedFolder == null)
		unsortedFolder = DEFAULT_UNSORTED_FOLDER;
	return getProject().getFolder(unsortedFolder);
}
/**
 * Performs an incremental build by leveraging the result of the
 * previous build.
 * @param delta describes how the resources have changed since
 * the last build
 * @exception Exception if the build can't proceed
 */
private void incrementalBuild(IResourceDelta delta) throws CoreException {
	IResource unsortedResource = delta.getResource();
	IPath unsortedFolderPath = getUnsortedFolder().getFullPath();
	IPath deltaPath = delta.getResource().getFullPath();

	boolean isUnderUnsortedFolder =
		unsortedFolderPath.isPrefixOf(deltaPath);

	boolean isOverUnsortedFolder =
		deltaPath.isPrefixOf(unsortedFolderPath);

	if(isUnderUnsortedFolder){
		int status = delta.getKind();
		int changeFlags = delta.getFlags();

		int type = unsortedResource.getType();

		IResource sortedResource =
			convertToSortedResource(unsortedResource);

		if(status == IResourceDelta.REMOVED){
			if(sortedResource.exists()){
				sortedResource.delete(false, null);
			}
		} else if(type == IResource.FILE && status == IResourceDelta.ADDED){
			build((IFile)unsortedResource);
		} else if(type == IResource.FILE && status == IResourceDelta.CHANGED && (changeFlags & IResourceDelta.CONTENT) != 0){
			build((IFile)unsortedResource);
		}
	}

	if(isUnderUnsortedFolder || isOverUnsortedFolder){
		IResourceDelta[] affectedChildren = delta.getAffectedChildren();
		for(int i = 0; i < affectedChildren.length; ++i){
			incrementalBuild(affectedChildren[i]);
		}
	}
}
/**
 * Returns true if the unsorted files are to be sorted in ascending
 * order, or false if they are to be sorted in descending order.
 * @return boolean
 */
private boolean isSortOrderAscending() {
	String sortOrder = (String) arguments.get(SORT_ORDER);
	return !DESCENDING.equals(sortOrder);
}
/**
 * Sorts the specified bytes in either ascending or descending order.
 * @param bytes
 * @param start position of first byte
 * @param end position of last byte
 * @param ascendingOrder true if the bytes are to be sorted in
 * ascending order, false if the bytes are to be sorted in
 * descending order
 */
private void quicksort(byte[] bytes, int start, int end, boolean ascendingOrder){
	if(start == end){
		return;
	}

	int pos = start;
	int mid = (start + end) / 2;
	swap(bytes, start, mid);

	for(int i = start + 1; i <= end; ++i){
		if(ascendingOrder && bytes[i] < bytes[start] ||
			!ascendingOrder && bytes[i] > bytes[start]){
			++pos;
			swap(bytes, pos, i);
		}
	}

	swap(bytes, start, pos);
	quicksort(bytes, start, pos, ascendingOrder);
	quicksort(bytes, pos + 1, end, ascendingOrder);
}
/**
 * Swaps the specified bytes in the given byte array.
 * @param bytes
 * @param pos1 the position of the first byte
 * @param pos2 the position of the second byte
 */
private void swap(byte[] bytes, int pos1, int pos2) {
	byte temp = bytes[pos1];
	bytes[pos1] = bytes[pos2];
	bytes[pos2] = temp;
}
/**
 * Performs an incremental build by leveraging the result of the
 * previous build.
 * @param delta describes how the resources have changed since
 * the last build
 * @exception CoreException if the build can't proceed
 */
protected boolean visitDelta(IResourceDelta delta) throws CoreException {
	try {
		IResource unsortedResource = delta.getResource();
		IPath unsortedFolderPath = getUnsortedFolder().getFullPath();
		IPath deltaPath = delta.getResource().getFullPath();

		boolean isUnderUnsortedFolder =
			unsortedFolderPath.isPrefixOf(deltaPath);

		boolean isOverUnsortedFolder =
			deltaPath.isPrefixOf(unsortedFolderPath);

		if(isUnderUnsortedFolder){
			int status = delta.getKind();
			int changeFlags = delta.getFlags();

			int type = unsortedResource.getType();

			IResource sortedResource =
				convertToSortedResource(unsortedResource);

			if(status == IResourceDelta.REMOVED){
				if(sortedResource.exists()){
					sortedResource.delete(false, null);
				}
			} else if(type == IResource.FILE && status == IResourceDelta.ADDED){
				build((IFile)unsortedResource);
			} else if(type == IResource.FILE && status == IResourceDelta.CHANGED && (changeFlags & IResourceDelta.CONTENT) != 0){
				build((IFile)unsortedResource);
			}
		}

		return isUnderUnsortedFolder || isOverUnsortedFolder;
	} catch(Exception e){
		throw new ResourceException(IResourceStatus.BUILD_FAILED, null, "Sort builder failed", e);
	}
}
public boolean wasAutoBuild() {
	return triggerForLastBuild == IncrementalProjectBuilder.AUTO_BUILD;
}
public boolean wasFullBuild() {
	return triggerForLastBuild == IncrementalProjectBuilder.FULL_BUILD;
}
public boolean wasIncrementalBuild() {
	return triggerForLastBuild == IncrementalProjectBuilder.INCREMENTAL_BUILD;
}
}
