/*
 * Created on Apr 7, 2004
 */
package org.cq2.delegator.examples.ComposedDocument;

import org.cq2.delegator.Delegator;
import junit.framework.TestCase;

public class ComposedDocumentTest extends TestCase {
	interface Document {
		String getName();
		String getUrl();
		String toHtml();
		String getTitle();
		String getBody();
	}

	public void testCreateDocument() {
		TextDocument doc = TextDocument.create("Best trips in Town", "bla bla bla...");
		// TODO: can call methods on doc?
		Context context = Context.create("book1", "http://books.com/besttrips");
		TextView view = TextView.create();
		Document cdoc = (Document) Delegator.compose(doc, context, view).cast(Document.class);
		assertNotNull(cdoc);
		String name = cdoc.getName();
		String url = cdoc.getUrl();
		String title = cdoc.getTitle();
		String body = cdoc.getBody();
		String html = cdoc.toHtml();
		assertEquals("book", name);
		assertEquals("Best trips in Town", title);
		assertEquals("http://books.com/besttrips", url);
		assertEquals("Chapter 1: Here we start. bla bla bla...", body);
		assertTrue(html.matches("<html><h1>Best.*</h1><a>bla.*</a></html>"));
	}
}
// TODO idea: Self.addFirst(), Self.remove(), Self.addLast, Self.replace()
