/*
 * Created on Apr 7, 2004
 */
package org.cq2.delegator.examples.composeddocument;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import junit.framework.TestCase;

import org.cq2.delegator.ISelf;
import org.cq2.delegator.ProxyMethodRegister;
import org.cq2.delegator.Self;

public class ComposedDocumentTest extends TestCase {
	public interface Document extends ISelf {
		String getName();
		String getUrl();
		String toHtml();
		String getTitle();
		String getBody();
	}

	public void testCreateDocumentSolveTHeBug() throws SecurityException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InvocationTargetException {
		Document doc = (Document) new Self().cast(Document.class);
        doc.add(TextDocument.class);
//
//		Method method = new Self().getClass().getDeclaredMethod("add", new Class[]{Class.class});
//
//        int identifier = ProxyMethodRegister.getInstance().getMethodIdentifier(method);
//        Method reflectMethod = new Self().composedClass.getReflectMethod(identifier);
//
//        Object[] args = new Object[]{TextDocument.class};
//        assertEquals(method, reflectMethod);
//        method.invoke(new Self(), args);
//        reflectMethod.invoke(new Self(), args);



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
	
//	public void testXXX() {
//        int[] componentIndexes = new int[5];
//        Arrays.fill(componentIndexes, -1);
//        componentIndexes[4] = 3;
//        int[] oldComponentIndexes = componentIndexes;
//        componentIndexes = new int[9];
//        System.arraycopy(oldComponentIndexes, 0, componentIndexes, 0,
//                oldComponentIndexes.length);
//        Arrays.fill(componentIndexes, oldComponentIndexes.length, componentIndexes.length, -1);
//        
//	}
}