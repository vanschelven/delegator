package org.cq2.delegator.examples.state;

import junit.framework.TestCase;

import org.cq2.delegator.Delegator;


public class StateTest extends TestCase {
	

	public void testRendersHeaderTags() {
		ParseState parseState = (ParseState) Delegator.extend(XMLRenderer.class,
				new Class[]{OutsideTable.class}).cast(ParseState.class);
		parseState.renderHeading("hello mum");
		String expectedRendering = "<h1>hello mum</h1>";
		String actualRendering = ((XMLRenderer)parseState.cast(XMLRenderer.class)).render();
			assertEquals(expectedRendering, actualRendering);
	}

	public void testThatWhenAfterTableRendersHeaderTags() {
		ParseState parseState = (ParseState) Delegator.extend(XMLRenderer.class,
				new Class[]{OutsideTable.class}).cast(ParseState.class);
		parseState.startTable();
		parseState.endTable();
		parseState.renderHeading("hello mum");
		String expectedRendering = "<table></table><h1>hello mum</h1>";
		String actualRendering = ((XMLRenderer)parseState.cast(XMLRenderer.class)).render();
		assertEquals(expectedRendering, actualRendering);
	}

	public void testThatWhenInTableIgnoreHeaderTags() {
		ParseState parseState = (ParseState) Delegator.extend(XMLRenderer.class,
				new Class[]{OutsideTable.class}).cast(ParseState.class);	
		parseState.startTable();
		parseState.renderHeading("hello mum");
		parseState.endTable();
		String expectedRendering = "<table>hello mum</table>";
		String actualRendering = ((XMLRenderer)parseState.cast(XMLRenderer.class)).render();
		assertEquals(expectedRendering, actualRendering);
	}

	public void testThatWhenInTableRendersThingTags() {
		ParseState parseState = (ParseState) Delegator.extend(XMLRenderer.class,
				new Class[]{OutsideTable.class}).cast(ParseState.class);
		parseState.startTable();
		parseState.renderHeading("whatever");
		parseState.endTable();
		String expectedRendering = "<table>whatever</table>";
		String actualRendering = ((XMLRenderer)parseState.cast(XMLRenderer.class)).render();
		assertEquals(expectedRendering, actualRendering);
	}

	public void testMapWithAttributes() {}
}
