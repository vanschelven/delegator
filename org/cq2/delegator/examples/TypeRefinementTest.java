/*
 * Created on Apr 2, 2004
 */
package org.cq2.delegator.examples;

import java.util.Date;
import java.util.HashMap;

import junit.framework.TestCase;

import org.cq2.delegator.ISelf;
import org.cq2.delegator.Self;

public class TypeRefinementTest extends TestCase {
	public static ISelf createMap() {
		return new Self(HashMap.class);
	}

	interface DateMap {
		void put(String key, Date value);
		Date get(String key);
	}

	public void testPutAndGetWithMoreSpecificTypes() {
		DateMap map = (DateMap) createMap().cast(DateMap.class);
		map.put("birthday", new Date(2345690));
		assertEquals(new Date(2345690), map.get("birthday"));
	}

	interface IntMap {
		void put(int key, int value);
		int get(int key);
	}

	public void testBoxing() {
		IntMap map = (IntMap) createMap().cast(IntMap.class);
		map.put(9, 16);
		assertEquals(16, map.get(9));
	}
}
