/*
 * Created on Apr 7, 2004
 */
package org.cq2.delegator.examples.composeddocument;

public abstract class TextView {
	public abstract String getBody();

	public abstract String getTitle();

	public String toHtml() {
		return "<html><h1>" + getTitle() + "</h1><a>" + getBody() + "</a></html>";
	}
}
