/*
 * Created on Apr 7, 2004
 */
package org.cq2.delegator.examples.composeddocument;

import junit.framework.TestCase;

import org.cq2.delegator.ISelf;
import org.cq2.delegator.Self;

public class ComposedDocumentTest extends TestCase {
	public interface Document extends ISelf {
		String getName();
		String getUrl();
		String toHtml();
		String getTitle();
		String getBody();
	}

	public void testCreateDocument() {
		Document doc = (Document) new Self().cast(Document.class);
		doc.add(TextDocument.class);
		doc.add(Context.class);
		doc.add(TextView.class);
		String name = doc.getName();
		String url = doc.getUrl();
		String title = doc.getTitle();
		String body = doc.getBody();
		String html = doc.toHtml();
		assertEquals("name", name);
		assertEquals("title", title);
		assertEquals("url", url);
		assertEquals("body", body);
		assertEquals("<html><h1>title</h1><a>body</a></html>", html);
	}
	
}