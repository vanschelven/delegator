package state;

import org.cq2.delegator.Delegator;
import junit.framework.TestCase;

public class StateTest extends TestCase {
	private XMLRenderer newRenderer() {
		
		Self self =  Delegator.extend(XMLRenderer.class,
				new Class[] {OutsideTable.class, InsideTable.class});
		return (XMLRenderer) self.cast(XMLRenderer.class);
	}

	public void testRendersHeaderTags() {
		XMLRenderer xmlRenderer = newRenderer();
		xmlRenderer.startHeading("hello mum");
		String expectedRendering = "<h1>hello mum</h1>";
		String actualRendering = xmlRenderer.render();
		assertEquals(expectedRendering, actualRendering);
	}

	public void testThatWhenAfterTableRendersHeaderTags() {
		XMLRenderer xmlRenderer = newRenderer();
		xmlRenderer.startTable();
		xmlRenderer.endTable();
		xmlRenderer.startHeading("hello mum");
		String expectedRendering = "<table></table><h1>hello mum</h1>";
		String actualRendering = xmlRenderer.render();
		assertEquals(expectedRendering, actualRendering);
	}

	public void testThatWhenInTableIgnoreHeaderTags() {
		XMLRenderer xmlRenderer = newRenderer();
		xmlRenderer.startTable();
		xmlRenderer.startHeading("hello mum");
		xmlRenderer.endTable();
		String expectedRendering = "<table></table>";
		String actualRendering = xmlRenderer.render();
		assertEquals(expectedRendering, actualRendering);
	}

	public void testThatWhenInTableRendersThingTags() {
		XMLRenderer xmlRenderer = newRenderer();
		xmlRenderer.startTable();
		xmlRenderer.startThing("whatever");
		xmlRenderer.endTable();
		String expectedRendering = "<table>whatever</table>";
		String actualRendering = xmlRenderer.render();
		assertEquals(expectedRendering, actualRendering);
	}
	
	public void testMultipleInheritanceCasting() {
		
		Self self = Delegator.extend(XMLRenderer.class,
				new Class[] {OutsideTable.class, MyClass.class });
		XMLRenderer renderer = (XMLRenderer) self.cast(XMLRenderer.class);
		assertEquals("multiple inheritance", renderer.bla());
		
		MyClass my = (MyClass) self.cast(MyClass.class);
		assertEquals("multiple inheritance", my.bla());
			
		
	}
	
	public static class MyClassB{
		public String bla(){
			return "bbbbbb";
		}
	}
	
	public void testBA(){
		Self self = Delegator.extend(MyClassB.class, new Class[]{MyClass.class});
		MyClassB b = (MyClassB) self.cast(MyClassB.class);
		assertEquals("bbbbbb",b.bla());
		MyClass a = (MyClass) self.cast(MyClass.class);
		assertEquals("bbbbbb",a.bla());
	}
	
	
	public void testMultiCast() {
			OutsideTable table = (OutsideTable) (Delegator.extend(XMLRenderer.class,
			new Class[] {OutsideTable.class, MyClass.class} )).cast(OutsideTable.class);
			assertNotNull(table);
	}			
}
