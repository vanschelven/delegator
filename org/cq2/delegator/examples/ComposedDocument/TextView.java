/*
 * Created on Apr 7, 2004
 */
package org.cq2.delegator.examples.ComposedDocument;

import org.cq2.delegator.Delegator;

public abstract class TextView {
	public static TextView create() {
		return (TextView) Delegator.create(TextView.class, new Object[0]);
	}

	// prototypes for methods from TextDocument
	public abstract String getBody();
	public abstract String getTitle();

	public String toHtml() {
		return "<html><h1>" + getTitle() + "</h1><a>" + getBody() + "</a></html>";
	}
}
