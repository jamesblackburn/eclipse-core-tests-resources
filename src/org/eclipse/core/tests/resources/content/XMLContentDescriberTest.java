/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.resources.content;

import java.io.*;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.content.IContentDescription;

/**
 * Ensures the XMLContentDescriber is able to handle encodings properly.
 */
public class XMLContentDescriberTest extends ContentTypeTest {
	private final static String ENCODED_TEXT = "\u1000\u20001\u3000\u4000\u5000\u6000\u7000\u8000\u9000\uA000";
	private final static String ENCODING_UTF16 = "UTF-16";
	private final static String ENCODING_UTF8 = "UTF-8";
	private final static String ENCODING_NOTSUPPORTED = "ENCODING_NOTSUPPORTED";
	private final static String ENCODING_INCORRECT = "<?ENCODING?>";
	private final static String ENCODING_EMPTY = "";

	public XMLContentDescriberTest(String name) {
		super(name);
	}

	public static Test suite() {
		return new TestSuite(XMLContentDescriberTest.class);
	}

	public void testEncodedContents1() throws Exception {
		checkEncodedContents(ENCODING_UTF16, ENCODING_UTF16, ENCODING_UTF16);
		checkEncodedContents(ENCODING_UTF8, ENCODING_UTF8, ENCODING_UTF8);

		checkEncodedContents(ENCODING_UTF16, ENCODING_UTF16, ENCODING_UTF8);
		checkEncodedContents(ENCODING_UTF8, ENCODING_UTF8, ENCODING_UTF16);

		checkEncodedContents(ENCODING_NOTSUPPORTED, ENCODING_NOTSUPPORTED, ENCODING_UTF8);
		checkEncodedContents(ENCODING_NOTSUPPORTED, ENCODING_NOTSUPPORTED, ENCODING_UTF16);
	}

	public void testEncodedContents2() throws Exception {
		checkEncodedContents2(ENCODING_UTF16, ENCODING_UTF16);
		checkEncodedContents2(ENCODING_UTF8, ENCODING_UTF8);
		checkEncodedContents2(ENCODING_NOTSUPPORTED, ENCODING_NOTSUPPORTED);
	}

	public void testEncodedContents3() throws Exception {
		boolean[][] flags = { {true, true}, {true, false}, {false, true}, {false, false}};

		IContentDescription description = null;
		for (int i = 0; i < flags.length; i++) {
			description = Platform.getContentTypeManager().getDescriptionFor(getInputStream(ENCODING_INCORRECT, ENCODING_UTF8, flags[i][0], flags[i][1]), "fake.xml", new QualifiedName[] {IContentDescription.CHARSET});
			assertNull("1.0", description);

			description = Platform.getContentTypeManager().getDescriptionFor(getInputStream(ENCODING_INCORRECT, ENCODING_UTF16, flags[i][0], flags[i][1]), "fake.xml", new QualifiedName[] {IContentDescription.CHARSET});
			assertNull("2.0", description);

			description = Platform.getContentTypeManager().getDescriptionFor(getReader(ENCODING_INCORRECT, flags[i][0], flags[i][1]), "fake.xml", new QualifiedName[] {IContentDescription.CHARSET});
			assertNull("3.0", description);
		}

		for (int i = 0; i < flags.length; i++) {
			description = Platform.getContentTypeManager().getDescriptionFor(getInputStream(ENCODING_EMPTY, ENCODING_UTF8, flags[i][0], flags[i][1]), "fake.xml", new QualifiedName[] {IContentDescription.CHARSET});
			assertNull("1.0", description);

			description = Platform.getContentTypeManager().getDescriptionFor(getInputStream(ENCODING_EMPTY, ENCODING_UTF16, flags[i][0], flags[i][1]), "fake.xml", new QualifiedName[] {IContentDescription.CHARSET});
			assertNull("2.0", description);

			description = Platform.getContentTypeManager().getDescriptionFor(getReader(ENCODING_EMPTY, flags[i][0], flags[i][1]), "fake.xml", new QualifiedName[] {IContentDescription.CHARSET});
			assertNull("3.0", description);
		}
	}

	public void testBug258208() throws Exception {
		IContentDescription description = Platform.getContentTypeManager().getDescriptionFor(getInputStream(ENCODING_EMPTY, ENCODING_UTF8, false, true), "fake.xml", new QualifiedName[] {IContentDescription.CHARSET});
		assertNull("1.0", description);

		// empty charset should not disable the xml content type
		checkEncodedContents(ENCODING_UTF16, ENCODING_UTF16, ENCODING_UTF16);
	}

	private void checkEncodedContents(String expectedEncoding, String encodingInContent, String encoding) throws Exception {
		boolean[][] flags = { {true, true}, {true, false}, {false, true}, {false, false}};

		IContentDescription description = null;
		for (int i = 0; i < flags.length; i++) {
			description = Platform.getContentTypeManager().getDescriptionFor(getInputStream(encodingInContent, encoding, flags[i][0], flags[i][1]), "fake.xml", new QualifiedName[] {IContentDescription.CHARSET});
			assertNotNull("1.0: " + flags[i][0] + " " + flags[i][1], description);
			assertEquals("1.1: " + flags[i][0] + " " + flags[i][1], Platform.PI_RUNTIME + ".xml", description.getContentType().getId());
			assertEquals("1.2: " + flags[i][0] + " " + flags[i][1], expectedEncoding, description.getProperty(IContentDescription.CHARSET));
		}
	}

	private void checkEncodedContents2(String expectedEncoding, String encodingInContent) throws Exception {
		boolean[][] flags = { {true, true}, {true, false}, {false, true}, {false, false}};

		IContentDescription description = null;
		for (int i = 0; i < flags.length; i++) {
			description = Platform.getContentTypeManager().getDescriptionFor(getReader(encodingInContent, flags[i][0], flags[i][1]), "fake.xml", new QualifiedName[] {IContentDescription.CHARSET});
			assertNotNull("1.0: " + flags[i][0] + " " + flags[i][1], description);
			assertEquals("1.1: " + flags[i], Platform.PI_RUNTIME + ".xml", description.getContentType().getId());
			assertEquals("1.2: " + flags[i], expectedEncoding, description.getProperty(IContentDescription.CHARSET));
		}
	}

	private InputStream getInputStream(String encodingInContent, String encoding, boolean encInNewLine, boolean encClosed) throws UnsupportedEncodingException {
		String content = "<?xml version=\"1.0\"" + (encInNewLine ? "\n" : "") + "encoding=\"" + encodingInContent + (encClosed ? "\"" : "") + "?><root attribute=\"" + ENCODED_TEXT + "\">";
		return new ByteArrayInputStream(content.getBytes(encoding));
	}

	private Reader getReader(String encodingInContent, boolean encInNewLine, boolean encClosed) {
		String content = "<?xml version=\"1.0\"" + (encInNewLine ? "\n" : "") + " encoding=\"" + encodingInContent + (encClosed ? "\"" : "") + "?><root attribute=\"" + ENCODED_TEXT + "\">";
		return new InputStreamReader(new ByteArrayInputStream(content.getBytes()));
	}
}
