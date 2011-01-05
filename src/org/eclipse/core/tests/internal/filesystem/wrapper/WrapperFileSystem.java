/*******************************************************************************
 * Copyright (c) 2006, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     James Blackburn (Broadcom Corp.)
 *******************************************************************************/
package org.eclipse.core.tests.internal.filesystem.wrapper;

import java.net.URI;
import java.net.URISyntaxException;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.provider.FileSystem;
import org.eclipse.core.runtime.*;
import org.eclipse.core.tests.harness.CoreTest;
import org.eclipse.core.tests.resources.ResourceTest;

/**
 * A simple file system implementation that acts as a wrapper around the
 * local file system.
 * <p>
 * Also allows tests to inject a custom FileSystem returning their own
 * special-purpose FileStore.  Tests can use {@link #setCustomFileSystem(FileSystem)}
 * to provide custom {@link WrapperFileStore} behaviour.
 * </p>
 */
public class WrapperFileSystem extends FileSystem {

	protected static final IFileStore NULL_ROOT = EFS.getNullFileSystem().getStore(Path.ROOT);

	private static final String SCHEME_WRAPPED = "wrapped";

	private static volatile WrapperFileSystem instance;

	/** Custom file-system instance */
	private static volatile FileSystem customFS;

	public static URI getBasicURI(URI wrappedURI) {
		Assert.isLegal(SCHEME_WRAPPED.equals(wrappedURI.getScheme()));
		return URI.create(wrappedURI.getQuery());
	}

	public static synchronized WrapperFileSystem getInstance() {
		if (instance != null)
			return instance;
		return instance = new WrapperFileSystem();
	}

	/**
	 * Set custom filesystem which may return overriden
	 * {@link IFileStore}s (using {@link WrapperFileStore}
	 * as a base, some functionality can be changed).
	 * @param fs filesystem, or null to use default {@link WrapperFileStore}
	 *        based implementation.
	 */
	public static void setCustomFileSystem(FileSystem fs) {
		getInstance().customFS = fs;
	}

	public static URI getWrappedURI(URI baseURI) {
		try {
			return new URI(SCHEME_WRAPPED, null, baseURI.getPath(), baseURI.toString(), null);
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(e.toString());
		}
	}

	public WrapperFileSystem() {
		instance = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.filesystem.provider.FileSystem#getStore(java.net.URI)
	 */
	public IFileStore getStore(URI uri) {
		Assert.isLegal(SCHEME_WRAPPED.equals(uri.getScheme()));

		// Delegate to customFS if set
		if (customFS != null)
			return customFS.getStore(uri);

		IFileStore baseStore;
		try {
			baseStore = EFS.getStore(getBasicURI(uri));
		} catch (CoreException e) {
			CoreTest.log(ResourceTest.PI_RESOURCES_TESTS, e);
			return NULL_ROOT;
		}
		return new WrapperFileStore(baseStore);
	}
}
