/*
 * Created on Apr 7, 2004
 */
package org.cq2.delegator.examples.ComposedDocument;

import org.cq2.delegator.Delegator;

public class Context {
	public static Context create(String name, String url) {
		return (Context) Delegator.create(Context.class, new Object[0]);
	}

	public String getName() {
		return "name";
	}
	
	public String getUrl() {
		return "url";
	}
}
