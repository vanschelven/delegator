/*
 Copyright (C) 2001 Erik J. Groeneveld, http://www.ejgroeneveld.com
 Copyright (C) 2002, 2003, 2004 Seek You Too B.V. the Netherlands. http://www.cq2.nl 
 */
package org.cq2.delegator.examples;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.cq2.delegator.Delegator;

public class AbstractMapExtensionTest extends TestCase {
	
	public static abstract class MyMap implements Map {
	
		public static MyMap create() {
			return (MyMap) new Delegator().createExtension(MyMap.class, AbstractMap.class);
		}
		
		public Set entrySet() {
			return Collections.singleton(new Map.Entry() {
				public Object getKey() {
					return "tree";
				}
				public Object getValue() {
					return "monkey";
				}
				public Object setValue(Object arg0) {
					return null;
				}
			});
		}
	}

	public void testCreate() {
		MyMap map = MyMap.create();
		Object value = map.get("tree");
		assertEquals("monkey", value);
	}
}
