package org.eclipse.core.tests.internal.watson;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * Used in conjunction with PluggableDeltaLogicTests
 */
public class PhantomElementData {
	String name;
	boolean isPhantom;
/**
 * Creates a new element info for either a phantom or real element
 */
public PhantomElementData(String name, boolean isPhantom) {
	this.name = name;
	this.isPhantom = isPhantom;
}
/**
 * For debugging
 */
public String toString() {
	StringBuffer buf = new StringBuffer("ElementData(");
	buf.append(isPhantom ? "Phantom, " : "Real, ");
	buf.append(name);
	buf.append(')');
	return buf.toString();
}
}
