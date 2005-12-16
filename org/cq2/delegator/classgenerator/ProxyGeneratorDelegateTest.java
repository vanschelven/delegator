/*
 Copyright (C) 2001 Erik J. Groeneveld, http://www.ejgroeneveld.com
 Copyright (C) 2002, 2003, 2004 Seek You Too B.V. the Netherlands. http://www.cq2.nl 
 */
package org.cq2.delegator.classgenerator;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;

import junit.framework.TestCase;

import org.cq2.delegator.InvocationHandlerWrapper;
import org.cq2.delegator.MethodRegister;
import org.cq2.delegator.MyInvocationHandler;
import org.cq2.delegator.Self;

//EMPTY STUB