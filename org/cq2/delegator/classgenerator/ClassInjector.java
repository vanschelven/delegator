/*
Copyright (C) 2001 Erik J. Groeneveld, http://www.ejgroeneveld.com
Copyright (C) 2002, 2003, 2004 Seek You Too B.V. the Netherlands. http://www.cq2.nl 
*/
package org.cq2.delegator.classgenerator;

import java.security.ProtectionDomain;

class ClassInjector extends ClassLoader {

	ClassInjector(ClassLoader parent) {
		super(parent);
	}
	
	Class inject(String className, byte[] classDef, ProtectionDomain domain) {
		return defineClass(className, classDef, 0, classDef.length, domain);
	}
}
