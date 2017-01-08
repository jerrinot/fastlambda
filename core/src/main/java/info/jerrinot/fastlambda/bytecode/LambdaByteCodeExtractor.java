package info.jerrinot.fastlambda.bytecode;


import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.ClassFile;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.ConstPool;
import javassist.bytecode.DuplicateMemberException;
import javassist.bytecode.ExceptionTable;
import javassist.bytecode.MethodInfo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import static com.hazelcast.nio.IOUtil.closeResource;

public class LambdaByteCodeExtractor {

    public MethodStub getMethodStub(Class<?> clazz, String methodToFind) throws IOException {
        ClassFile classFile = getClassFile(clazz);

        MethodInfo method = classFile.getMethod(methodToFind);
        byte[] constantPool = getConstantPool(method);

        CodeAttribute codeAttribute = method.getCodeAttribute();
        byte[] bytecode = codeAttribute.getCode();

        String descriptor = method.getDescriptor();

        return new MethodStub(bytecode, constantPool, descriptor, codeAttribute.getMaxLocals(), methodToFind);
    }

    public byte[] generateClass(String classname, Class<?>[] interfaces, MethodStub methodStub) throws IOException, CannotCompileException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, BadBytecode, NotFoundException {
        ClassPool defaultPool = ClassPool.getDefault();
        CtClass ctClass = defaultPool.makeClass(classname);

        for (Class<?> clazz : interfaces) {
            CtClass ctInterface = defaultPool.getCtClass(clazz.getName());
            ctClass.addInterface(ctInterface);
        }

        ConstPool methodConstantPool = getConstantPool(methodStub);
        String methodStubName = methodStub.getName();
        MethodInfo methodInfo = new MethodInfo(methodConstantPool, methodStubName, methodStub.getDescriptor());
        ExceptionTable et = new ExceptionTable(methodConstantPool);
        CodeAttribute ca = new CodeAttribute(methodConstantPool, 0, methodStub.getMaxLocals(), methodStub.getBytecode(), et);
        ca.computeMaxStack();
        methodInfo.addAttribute(ca);
        ConstPool constPool = ctClass.getClassFile().getConstPool();
        replaceConstantPoolInMethodInfo(constPool, methodInfo);

        CtMethod actualMethod = CtMethod.make(methodInfo, ctClass);
        actualMethod.setModifiers(Modifier.PUBLIC | Modifier.STATIC);
        ctClass.addMethod(actualMethod);

        CtConstructor defaultConstructor = CtNewConstructor.defaultConstructor(ctClass);
        ctClass.addConstructor(defaultConstructor);

        Method samMethod = findSAM(interfaces);
        Class<?> returnType = samMethod.getReturnType();
        String desc = returnType.getTypeName();

        String signature = "public " + desc + " " + samMethod.getName()+"(";
        Class<?>[] parameterTypes = samMethod.getParameterTypes();
        String separator = "";
        for (int i = 0 ; i < parameterTypes.length; i++) {
            signature += separator;
            Class<?> parameterType = parameterTypes[i];
            signature += (parameterType.getTypeName() + " arg" + i);
            separator = ", ";
        }
        signature += ")";
        boolean isVoid = returnType == Void.class;
        String body = isVoid ? "" : "return ";
        body += methodStubName;
        body += "(";
        separator = "";
        CtClass[] actualMethodParameterTypes = actualMethod.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            CtClass actualMethodParameterType = actualMethodParameterTypes[i];
            String actualTypeName = actualMethodParameterType.getName();
            body += separator;
            body += "(" + actualTypeName + ")";
            body += ("arg" + i);
            separator = ", ";
        }
        body += ");";
        String methodString = signature + "{ " + body + " }";
        CtMethod delegatingMethod = CtNewMethod.make(methodString, ctClass);
        ctClass.addMethod(delegatingMethod);

        ctClass.getClassFile().setMajorVersion(ClassFile.JAVA_6);

        return ctClass.toBytecode();
    }

    private Method findSAM(Class<?>[] classes) {
        for (Class<?> clazz : classes) {
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                int modifiers = method.getModifiers();
                if (java.lang.reflect.Modifier.isAbstract(modifiers)) {
                    return method;
                }
            }
        }
        throw new AssertionError("None of these classes is SAM: " + Arrays.toString(classes));
    }

    private void replaceConstantPoolInMethodInfo(ConstPool constPool, MethodInfo methodInfo) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, DuplicateMemberException {
        Method compactMethod = MethodInfo.class.getDeclaredMethod("compact", ConstPool.class);
        compactMethod.setAccessible(true);
        compactMethod.invoke(methodInfo, constPool);
    }

    private ConstPool getConstantPool(MethodStub methodStub) throws IOException {
        byte[] constantPoolBytes = methodStub.getConstantPool();
        DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(constantPoolBytes));
        return new ConstPool(dataInputStream);
    }

    private ClassFile getClassFile(Class<?> clazz) throws IOException {
        byte[] clazzBytecode = getByteCodeOf(clazz);
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(clazzBytecode));
        return new ClassFile(dis);
    }

    private byte[] getConstantPool(MethodInfo method) throws IOException {
        ConstPool constPool = method.getConstPool();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        constPool.write(dos);
        return baos.toByteArray();
    }


    private byte[] getByteCodeOf(Class<?> c) {
        InputStream input = null;
        try {
            input = c.getResourceAsStream('/' + c.getName().replace('.', '/')+ ".class");
            byte[] result = new byte[input.available()];
            input.read(result);
            return result;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } finally {
            closeResource(input);
        }
    }
}
