/*
 Copyright (C) 2001 Erik J. Groeneveld, http://www.ejgroeneveld.com
 Copyright (C) 2002, 2003, 2004 Seek You Too B.V. the Netherlands. http://www.cq2.nl 
 *
 * Created on Mar 17, 2004
 */
package org.cq2.delegator.examples.observer;


public interface Observer {
	void notifyChanged(Object implementation);
}
