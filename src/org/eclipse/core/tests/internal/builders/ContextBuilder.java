/*******************************************************************************
 * Copyright (c) 2010, 2011 Broadcom Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Broadcom Corporation - initial API and implementation
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

	/** Stores IBuildConfiguration -> ContextBuilder */
	private static HashMap<IBuildConfiguration, ContextBuilder> builders = new HashMap<IBuildConfiguration, ContextBuilder>();
	/** The context information for the last run of this builder */
	IBuildContext contextForLastBuild = null;
	/** The trigger for the last run of this builder */
	int triggerForLastBuild = 0;

	private boolean getRuleCalledForLastBuild = false;
	private IBuildContext contextForLastBuildInGetRule = null;

	private IBuildConfiguration buildConfigurationForLastBuild = null;
	private IBuildConfiguration buildConfigurationForLastBuildInGetRule = null;

	public ContextBuilder() {
	}

	public static ContextBuilder getBuilder(IBuildConfiguration variant) {
		return builders.get(variant);
	}

	public static IBuildContext getContext(IBuildConfiguration variant) {
		if (!builders.containsKey(variant))
			return null;
		return getBuilder(variant).contextForLastBuild;
	}

	public static boolean checkValid() {
		for (Iterator<ContextBuilder> it = builders.values().iterator(); it.hasNext();) {
			ContextBuilder builder = it.next();
			if (builder.getRuleCalledForLastBuild && !builder.contextForLastBuild.equals(builder.contextForLastBuildInGetRule))
				return false;
			if (builder.getRuleCalledForLastBuild && !builder.buildConfigurationForLastBuild.equals(builder.buildConfigurationForLastBuildInGetRule))
				return false;
		}
		return true;
	}

	public static void clearStats() {
		for (Iterator<ContextBuilder> it = builders.values().iterator(); it.hasNext();) {
			ContextBuilder builder = it.next();
			builder.contextForLastBuild = null;
			builder.contextForLastBuildInGetRule = null;
			builder.buildConfigurationForLastBuild = null;
			builder.buildConfigurationForLastBuildInGetRule = null;
			builder.getRuleCalledForLastBuild = false;
			builder.triggerForLastBuild = 0;
		}
	}

	protected void startupOnInitialize() {
		builders.put(getBuildConfig(), this);
	}

	protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) throws CoreException {
		contextForLastBuild = getContext();
		triggerForLastBuild = kind;
		buildConfigurationForLastBuild = getBuildConfig();
		return super.build(kind, args, monitor);
	}

	protected void clean(IProgressMonitor monitor) throws CoreException {
		super.clean(monitor);
		contextForLastBuild = getContext();
		triggerForLastBuild = IncrementalProjectBuilder.CLEAN_BUILD;
		buildConfigurationForLastBuild = getBuildConfig();
	}

	/*
	 * (non-Javadoc)
	 * @see IncrementalProjectBuilder#getRule(int, Map)
	 */
	public ISchedulingRule getRule(int kind, Map<String, String> args) {
		getRuleCalledForLastBuild = true;
		contextForLastBuildInGetRule = getContext();
		buildConfigurationForLastBuildInGetRule = getBuildConfig();
		return super.getRule(kind, args);
	}
}