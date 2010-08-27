/*******************************************************************************
 * Copyright (c) 2010 Broadcom Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Alex Collins (Broadcom Corp.) - initial implementation
 *******************************************************************************/
package org.eclipse.core.tests.internal.builders;

import java.util.*;
import junit.framework.Assert;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * A builder used that stores statistics, such as which target was built, per project variant.
 */
public class VariantBuilder extends TestBuilder {
	public static final String BUILDER_NAME = "org.eclipse.core.tests.resources.variantbuilder";

	/** Stores IProjectVariant -> VariantBuilder */
	private static HashMap builders = new HashMap();
	/** Order in which builders have been run */
	static List buildOrder = new ArrayList();

	// Per builder instance stats
	int triggerForLastBuild;
	IResourceDelta deltaForLastBuild;
	int buildCount;

	public VariantBuilder() {
	}

	public static VariantBuilder getBuilder(IProjectVariant variant) {
		return (VariantBuilder) builders.get(variant);
	}

	public static void clearBuildOrder() {
		buildOrder = new ArrayList();
	}

	public static void clearStats() {
		for (Iterator it = builders.values().iterator(); it.hasNext();) {
			VariantBuilder builder = (VariantBuilder) it.next();
			builder.buildCount = 0;
			builder.triggerForLastBuild = 0;
			builder.deltaForLastBuild = null;
		}
	}

	protected void startupOnInitialize() {
		builders.put(getProjectVariant(), this);
		buildCount = 0;
	}

	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
		buildCount++;
		triggerForLastBuild = kind;
		deltaForLastBuild = getDelta(getProject());
		buildOrder.add(getProjectVariant());
		return super.build(kind, args, monitor);
	}

	protected void clean(IProgressMonitor monitor) throws CoreException {
		super.clean(monitor);
		IResourceDelta delta = getDelta(getProject());
		Assert.assertNull(delta);
		buildCount++;
		triggerForLastBuild = IncrementalProjectBuilder.CLEAN_BUILD;
	}
}
