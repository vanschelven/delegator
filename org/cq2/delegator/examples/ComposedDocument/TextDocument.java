/*
 * Created on Apr 7, 2004
 */
package org.cq2.delegator.examples.ComposedDocument;

import org.cq2.delegator.Delegator;

public class TextDocument {
	public static TextDocument create(String title, String body) {
		return (TextDocument) Delegator.instanceOf(TextDocument.class);
	}

	public String getTitle() {
		return "title";
	}

	public String getBody() {
		return "body";
	}
}
