/*
 Copyright (C) 2001 Erik J. Groeneveld, http://www.ejgroeneveld.com
 Copyright (C) 2002, 2003, 2004 Seek You Too B.V. the Netherlands. http://www.cq2.nl 
 */
package org.cq2.delegator.examples;

import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;
import org.cq2.delegator.Delegator;

public class HashMapExtensionTest extends TestCase {
	public static abstract class MyMap implements Map {
		public static MyMap create() throws Exception {
			return (MyMap) Delegator.extend(MyMap.class, new Class[]{HashMap.class});
		}

		public Object get(Object key) {
			return "ha ha ha, this method is overridden!";
		}
	}

	public void testCreateMyMap() throws Exception {
		MyMap map = MyMap.create();
		assertNotNull(map);
		map.put("monkey", "fish");
		assertTrue(map.containsKey("monkey"));
		assertTrue(map.containsValue("fish"));
		assertEquals("fish", map.remove("monkey"));
		assertEquals("ha ha ha, this method is overridden!", map
				.get("something completely nonsense"));
	}
}
