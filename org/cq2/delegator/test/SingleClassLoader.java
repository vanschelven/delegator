package org.cq2.delegator.test;

class SingleClassLoader extends ClassLoader {

    private byte[] classAsBytes;

    public SingleClassLoader(byte [] classAsBytes) {
        super();
        this.classAsBytes = classAsBytes;
    }
    
    protected Class findClass(String name) throws ClassNotFoundException {
        return defineClass(null, classAsBytes, 0, classAsBytes.length);
    }
    
    public Class loadClass() throws ClassNotFoundException {
        return loadClass("intentional nonsense here");
    }
            
}
