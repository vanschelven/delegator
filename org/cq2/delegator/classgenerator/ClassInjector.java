/*
 Copyright (C) 2001 Erik J. Groeneveld, http://www.ejgroeneveld.com
 Copyright (C) 2002, 2003, 2004 Seek You Too B.V. the Netherlands. http://www.cq2.nl 
 */
package org.cq2.delegator.classgenerator;

import java.security.ProtectionDomain;

public class ClassInjector extends ClassLoader {
	public ClassInjector(ClassLoader parent) {
		super(parent);
	}

	public Class inject(String className, byte[] classDef, ProtectionDomain domain) {
		return defineClass(className, classDef, 0, classDef.length, domain);
	}

	public static ClassInjector create() {
		return ClassInjector.create(getSystemClassLoader());
	}

	public static ClassInjector create(ClassLoader parent) {
		if (parent instanceof ClassInjector) {
			return (ClassInjector) parent;
		}
		return new ClassInjector(parent);
	}

	protected Class findClass(String classname) throws ClassNotFoundException {
		if (classname.endsWith("$component")) {
			return ProxyGenerator.injectComponentClass(this, loadClass(classname.substring(0,
					classname.length() - 10)));
		}
		else if (classname.startsWith("component$")) {
			return ProxyGenerator.injectComponentClass(this, loadClass(classname.substring(10)));
		}
		else if (classname.endsWith("$proxy")) {
			return ProxyGenerator.injectProxyClass(this, loadClass(classname.substring(0, classname
					.length() - 6)));
		}
		else if (classname.startsWith("proxy$")) {
			return ProxyGenerator.injectProxyClass(this, loadClass(classname.substring(6)));
		}
		throw new ClassNotFoundException(classname);
	}
}
