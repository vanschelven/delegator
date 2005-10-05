package org.cq2.delegator.classgenerator;

import org.cq2.delegator.ComponentMethodGenerator;
import org.cq2.delegator.ProxyMethodGenerator;
import org.cq2.delegator.ProxyMethodRegister;

public class MethodClassLoader extends ClassLoader {

    public MethodClassLoader(ClassLoader parent) {
        super(parent);
    }

    protected Class findClass(String name) throws ClassNotFoundException {
    //    System.out.println(name + " loaded by " + this);
        System.out.println("***");
        System.out.println(name);
        System.out.println(this);
        System.out.println(getParent());
        System.out.println(getParent().getParent());
        String prefix = "org.cq2.delegator.ProxyMethod";
        if (name.startsWith(prefix) && !name.equals(prefix)) {
            String postfix = name.substring(prefix.length());
            int identifier = Integer.parseInt(postfix);
            byte[] bytes = new ProxyMethodGenerator(identifier,
                    ProxyMethodRegister.getInstance().getMethod(identifier))
                    .generate();
            return defineClass(name, bytes, 0, bytes.length);
        }
        prefix = "org.cq2.delegator.ComponentMethod";
        if (name.startsWith(prefix) && !name.equals(prefix)) {
            String postfix = name.substring(prefix.length());
            int methodIdentifier = Integer.parseInt(postfix.substring(0, postfix.indexOf('_')));
            int componentIdentifier = Integer.parseInt(postfix.substring(postfix.indexOf('_') + 1, postfix.length()));
            byte[] bytes =  new ComponentMethodGenerator(methodIdentifier, componentIdentifier).generate();
            return defineClass(name, bytes, 0, bytes.length);
        }
     //   System.out.println("fail");
        throw new ClassNotFoundException(name);
    }

}