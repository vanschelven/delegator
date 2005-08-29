package org.cq2.delegator;

import java.lang.reflect.Method;

import org.apache.bcel.Constants;
import org.apache.bcel.Repository;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.ReferenceType;
import org.apache.bcel.generic.Type;
import org.cq2.delegator.classgenerator.ClassGenerator;

public class MethodWrapperGenerator implements Constants {

    protected final ClassGen classGen;
    private final InstructionFactory instrFact;
    private final InstructionList instrList;
    private final ConstantPoolGen constPool;
    private MethodGen methodGen;
    private JavaClass methodWrapperClass;
    private Class componentClass;
    private static int unique;

    //TODO inzetten indien noodzakelijk. (ja dat is noodzakelijk maar niet direct)
    public static class Cache {
        
        private static Cache instance;
        private Class clazz;

        static Cache getInstance() {
            if (instance == null) instance = new Cache();
            return instance;
        }
        
        public Class get() {
            return clazz;
        }
        
        public void set(Class clazz) {
            this.clazz = clazz;
        }
        
    }
    
    public MethodWrapperGenerator(Class componentClass, String methodName) {
        this.componentClass = componentClass;
        methodWrapperClass = Repository.lookupClass(MethodWrapper.class);
        String superclassName = MethodWrapper.class.getName();
        
        classGen = new ClassGen(superclassName + getUnique(), superclassName, "", ACC_PUBLIC, new String[]{});
        constPool = classGen.getConstantPool();
        instrFact = new InstructionFactory(classGen, constPool);
        instrList = new InstructionList();
        addDefaultConstructor();
        add_i_invoke(methodName);
    }
    
    //TODO een ding dat alle tests twee keer draait zodat we zien of de cache toestand uberhaubt werkt.
    
    private int getUnique() {
        return unique++;
    }

    public MethodWrapper generate(int uniqueMethodIdentifier) {
        try {
            ClassLoader parentClassLoader = componentClass.getClassLoader();
            if (parentClassLoader == null) parentClassLoader = ClassLoader.getSystemClassLoader();
            Class clazz = new SingleNamedClassLoader("MethodWrapperXX", classGen.getJavaClass().getBytes(), parentClassLoader).loadClass();
            Cache.getInstance().set(clazz);
            MethodWrapper result = (MethodWrapper) clazz.newInstance();
            return result;
        } catch (Exception e) {
           throw new RuntimeException(e);
        }
    }

    private void addDefaultConstructor() {
        MethodGen methodGen = new MethodGen(ACC_PUBLIC, Type.VOID,
                Type.NO_ARGS, new String[] {}, "<init>", classGen
                        .getClassName(), instrList, constPool);
        createLoadThis();
        instrList.append(instrFact.createInvoke(classGen.getSuperclassName(),
                "<init>", Type.VOID, Type.NO_ARGS, Constants.INVOKESPECIAL));
        instrList.append(InstructionFactory.createReturn(Type.VOID));
        methodGen.setMaxStack();
        methodGen.setMaxLocals();
        classGen.addMethod(methodGen.getMethod());
        instrList.dispose();
    }

    private void createLoadThis() {
        instrList.append(InstructionFactory.createLoad(Type.OBJECT, 0));
    }

    //TODO distinguish between calls to generated and non-generated components (Automatic Forwarding TEst)
    private void add_i_invoke(String methodName) {
        try {
            Method superReflectMethod = MethodWrapper.class.getDeclaredMethod("i_invoke", new Class[] {Object.class});
            org.apache.bcel.classfile.Method superBCELMethod = methodWrapperClass.getMethod(superReflectMethod);
            org.apache.bcel.classfile.Method method = new org.apache.bcel.classfile.Method(superBCELMethod);
            methodGen = new MethodGen(method.getModifiers(), Type.INT, new Type[]{Type.OBJECT}, new String[]{"component"}, method.getName(), classGen.getClassName(), instrList, constPool);
            
            //((component$java.util.Vector) component).size()
            instrList.append(InstructionFactory.createLoad(Type.OBJECT, 1));
            instrList.append(instrFact.createCheckCast((ReferenceType) Type.getType(componentClass)));
            instrList.append(instrFact.createInvoke(componentClass.getName(), methodName, Type.INT, new Type[]{}, Constants.INVOKEVIRTUAL));
            instrList.append(InstructionFactory.createReturn(Type.INT));
            
            methodGen.setMaxStack();
            methodGen.setMaxLocals();

            classGen.addMethod(methodGen.getMethod());
    
            instrList.dispose();
        } catch (Exception e) {
            throw new RuntimeException(e.getCause());
        }
    }

}
