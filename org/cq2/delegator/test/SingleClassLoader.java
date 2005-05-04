package org.cq2.delegator.test;

class SingleClassLoader extends ClassLoader {

    private byte[] classAsBytes;

    public SingleClassLoader(byte [] classAsBytes) {
        super();
        this.classAsBytes = classAsBytes;
    }
    
    private Class inject(byte[] classDef) {
        return defineClass(null, classDef, 0, classDef.length);
    }

    protected Class findClass(String name) throws ClassNotFoundException {
        return defineClass(null, classAsBytes, 0, classAsBytes.length);
    }
            
}
