/*
 * Created on Apr 7, 2004
 */
package org.cq2.delegator.examples.ComposedDocument;

import junit.framework.TestCase;

import org.cq2.delegator.handlers.ISelf;
import org.cq2.delegator.handlers.Self;

public class ComposedDocumentTest extends TestCase {
	interface Document extends ISelf {
		String getName();
		String getUrl();
		String toHtml();
		String getTitle();
		String getBody();
	}

	public void testCreateDocument() {
		Document doc = (Document) new Self(Document.class).cast(Document.class);
		doc.add(TextDocument.class, new Object[] {"Best trips in Town", "bla bla bla..."});
		doc.add(Context.class, new Object[] {"book1", "http://books.com/besttrips"});
		doc.add(TextView.class);
		String name = doc.getName();
		String url = doc.getUrl();
		String title = doc.getTitle();
		String body = doc.getBody();
		String html = doc.toHtml();
		assertEquals("book", name);
		assertEquals("Best trips in Town", title);
		assertEquals("http://books.com/besttrips", url);
		assertEquals("Chapter 1: Here we start. bla bla bla...", body);
		assertTrue(html.matches("<html><h1>Best.*</h1><a>bla.*</a></html>"));
	}
}