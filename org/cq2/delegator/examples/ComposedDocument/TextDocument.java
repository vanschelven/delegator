/*
 * Created on Apr 7, 2004
 */
package org.cq2.delegator.examples.ComposedDocument;

import org.cq2.delegator.Delegator;

public class TextDocument {
	public static TextDocument create(String title, String body) {
		return (TextDocument) Delegator.create(TextDocument.class, new Object[0]);
	}

	public String getTitle() {
		return "title";
	}

	public String getBody() {
		return "body";
	}
}
