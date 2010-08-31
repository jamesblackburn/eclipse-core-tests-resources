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
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

/**
 * A builder used that stores the context information passed to it.
 */
public class ContextBuilder extends TestBuilder {
	public static final String BUILDER_NAME = "org.eclipse.core.tests.resources.contextbuilder";

	/** Stores IProjectVariant -> ContextBuilder */
	private static HashMap builders = new HashMap();
	/** The context information for the last run of this builder */
	IBuildContext contextForLastBuild = null;
	/** The trigger for the last run of this builder */
	int triggerForLastBuild = 0;

	private boolean getRuleCalledForLastBuild = false;
	private IBuildContext contextForLastBuildInGetRule = null;

	private IProjectVariant projectVariantForLastBuild = null;
	private IProjectVariant projectVariantForLastBuildInGetRule = null;

	public ContextBuilder() {
	}

	public static ContextBuilder getBuilder(IProjectVariant variant) {
		return (ContextBuilder) builders.get(variant);
	}

	public static IBuildContext getContext(IProjectVariant variant) {
		return getBuilder(variant).contextForLastBuild;
	}

	public static boolean checkValid() {
		for (Iterator it = builders.values().iterator(); it.hasNext();) {
			ContextBuilder builder = (ContextBuilder) it.next();
			if (builder.getRuleCalledForLastBuild && !builder.contextForLastBuild.equals(builder.contextForLastBuildInGetRule))
				return false;
			if (builder.getRuleCalledForLastBuild && !builder.projectVariantForLastBuild.equals(builder.projectVariantForLastBuildInGetRule))
				return false;
		}
		return true;
	}

	public static void clearStats() {
		for (Iterator it = builders.values().iterator(); it.hasNext();) {
			ContextBuilder builder = (ContextBuilder) it.next();
			builder.contextForLastBuild = null;
			builder.contextForLastBuildInGetRule = null;
			builder.projectVariantForLastBuild = null;
			builder.projectVariantForLastBuildInGetRule = null;
			builder.getRuleCalledForLastBuild = false;
			builder.triggerForLastBuild = 0;
		}
	}

	protected void startupOnInitialize() {
		builders.put(getProjectVariant(), this);
	}

	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
		contextForLastBuild = getContext();
		triggerForLastBuild = kind;
		projectVariantForLastBuild = getProjectVariant();
		return super.build(kind, args, monitor);
	}

	protected void clean(IProgressMonitor monitor) throws CoreException {
		super.clean(monitor);
		contextForLastBuild = getContext();
		triggerForLastBuild = IncrementalProjectBuilder.CLEAN_BUILD;
		projectVariantForLastBuild = getProjectVariant();
	}

	/*
	 * (non-Javadoc)
	 * @see IncrementalProjectBuilder#getRule(int, Map)
	 */
	public ISchedulingRule getRule(int kind, Map args) {
		getRuleCalledForLastBuild = true;
		contextForLastBuildInGetRule = getContext();
		projectVariantForLastBuildInGetRule = getProjectVariant();
		return super.getRule(kind, args);
	}
}
