/*
 Copyright (C) 2001 Erik J. Groeneveld, http://www.ejgroeneveld.com
 Copyright (C) 2002, 2003, 2004 Seek You Too B.V. the Netherlands. http://www.cq2.nl 
 */
package org.cq2.delegator.handlers;

import java.io.DataInput;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.cq2.delegator.Delegator;

/**
 * @author ejgroene
 *
 */
public class ComposerTest extends TestCase {

	private Object keyRef = null;
	private Object valueRef = null;
	private Object returnVal = new Object();

	public Object put(Object key, Object value) {
		keyRef = key;
		valueRef = value;
		return returnVal;
	}

	public void testMapDelegator() {
		Map map = (Map) Delegator.forInterface(Map.class, this);
		Object key = new Object();
		Object value = new Object();
		Object result = map.put(key, value);
		assertEquals(key, keyRef);
		assertEquals(value, valueRef);
		assertEquals(result, returnVal);
	}

	public void testNoSuchMethod() {
		Set set = (Set) Delegator.forInterface(Set.class, this);
		try {
			set.clear();
			fail("Should throw NoSuchMethodError()");
		} catch (NoSuchMethodError e) {}
	}

	public boolean execute(String query) throws SQLException {
		throw new SQLException();
	}

	public void testException() throws SQLException {
		Statement statement = (Statement) Delegator.forInterface(Statement.class, this);
		try {
			statement.execute("niks");
			fail();
		} catch (SQLException e) {} catch (Throwable e) {
			e.printStackTrace();
			fail("Invalid exception!");
		}
	}

	public void testFouteReturnType() {
		Comparator comp = (Comparator) Delegator.forInterface(Comparator.class, this);
		try {
			comp.compare(null, null);
			fail();
		} catch (NoSuchMethodError e) {}
	}

	/**
	 * @see Comparator.compare()
	 */
	public long compare(Object a, Object b) {
		return 0;
	}

	public void testMissingExceptionType() throws Exception {
		DataInput input = (DataInput) Delegator.forInterface(DataInput.class, this);
		try {
			input.readByte();
			fail("exception types should not match");
		} catch (NoSuchMethodError e) {}
	}

	/**
	 * @see DataInput.readByte()  Intentionally without throws IOException
	 */
	public byte readByte() {
		return 0;
	}

	public void testPublicOnly() {
		List list = (List) Delegator.forInterface(List.class, this);
		try {
			list.add(new Object());
			fail();
		} catch (NoSuchMethodError e) {}
	}

	public void addBatch(String query) { // should throws SQLException
	}

	public int hashCode() {
		return 1;
	}

	public void testHashCode() {
		List list = (List) Delegator.forInterface(List.class, this);
		try {
			list.hashCode();
			fail();
		} catch (Error e) {}
	}
}