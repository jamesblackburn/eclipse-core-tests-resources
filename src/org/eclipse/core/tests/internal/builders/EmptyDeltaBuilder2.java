/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.internal.builders;

/**
 * A builder that has the callOnEmptyDelta attribute set to true in the
 * builder extension.
 */
public class EmptyDeltaBuilder2 extends TestBuilder {
	public static final String BUILDER_NAME = "org.eclipse.core.tests.resources.emptydeltabuilder2";

	/**
	 * The most recently created instance
	 */
	protected static EmptyDeltaBuilder2 singleton;

	/**
	 * Returns the most recently created instance.
	 */
	public static EmptyDeltaBuilder2 getInstance() {
		return singleton;
	}

	public EmptyDeltaBuilder2() {
		singleton = this;
	}
}
