package org.cq2.delegator;

class SingleNamedClassLoader extends ClassLoader {

    private byte[] classAsBytes;
    private final String name;

    public SingleNamedClassLoader(String name, byte [] classAsBytes, ClassLoader parent) {
        super(parent);
        this.name = name;
        this.classAsBytes = classAsBytes;
    }
    
    protected Class findClass(String name) throws ClassNotFoundException {
        if (name.equals(this.name))
            return defineClass(null, classAsBytes, 0, classAsBytes.length);
        return getParent().loadClass(name);
    }
    
    public Class loadClass() throws ClassNotFoundException {
        return loadClass(name);
    }
            
}
