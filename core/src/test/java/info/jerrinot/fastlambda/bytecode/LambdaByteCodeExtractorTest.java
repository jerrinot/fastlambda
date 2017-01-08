//package info.jerrinot.fastlambda.bytecode;
//
//import info.jerrinot.fastlambda.Utils;
//import javassist.ClassPool;
//import javassist.NotFoundException;
//import org.junit.Test;
//
//import java.io.Serializable;
//import java.lang.invoke.SerializedLambda;
//import java.lang.reflect.Field;
//import java.lang.reflect.Method;
//import java.util.Arrays;
//import java.util.function.Function;
//
//import static org.junit.Assert.*;
//
//public class LambdaByteCodeExtractorTest {
//
//    @Test
//    public void getMethodStub() throws Exception {
//        LambdaByteCodeExtractor extractor = new LambdaByteCodeExtractor();
//        MethodStub methodStub = extractor.getMethodStub(LambdaByteCodeExtractorTest.class, "getMethodStub");
//
//        assertNotNull(methodStub.getBytecode());
//        assertNotNull(methodStub.getConstantPool());
//    }
//
//    @Test
//    public void foo() throws NotFoundException {
//        ClassPool cp = ClassPool.getDefault();
//        String captured = "0";
//        Serializable r = (Function<String, Integer> & Serializable) (s) -> Integer.parseInt(s + captured);
//
//        inspectObject(r);
//
//    }
//
//    private void inspectObject(Object r) {
//        Class<?> clazz = r.getClass();
//        System.out.println("Lambda type: " + r.getClass());
//        Field[] fields = clazz.getDeclaredFields();
//        for (Field field : fields) {
//            field.setAccessible(true);
//            System.out.println("Field: " + field.getName() + ", type: " + field.getType());
//        }
//
//        Method[] methods = clazz.getDeclaredMethods();
//        for (Method method : methods) {
//            method.setAccessible(true);
//            System.out.println("Method: " + method.getName()
//                    + " parameter types: " + Arrays.toString(method.getParameterTypes())
//                    + " return type: " + method.getReturnType());
//        }
//    }
//
//    @Test
//    public void testCopyLambdaMethod() throws Exception {
//        Serializable r = (Function<Integer, String> & Serializable) (i) -> "Hello Lambda World: " + i;
//        System.out.println("Lambda class: " + r.getClass());
//
//        String methodNameToCopy = translateLambdaIntoMethodName(r);
//        System.out.println("Lambda method:" + methodNameToCopy);
//
//        LambdaByteCodeExtractor extractor = new LambdaByteCodeExtractor();
//        MethodStub methodStub = extractor.getMethodStub(LambdaByteCodeExtractorTest.class, methodNameToCopy);
//
//        String generatedClassName = "info.jerrinot.fastlambda.bytecode.GeneratedLambdaByteCodeExtractor";
//        byte[] aClass = extractor.generateClass(generatedClassName, r.getClass().getInterfaces(), methodStub);
//
//        ClassLoader classLoader = new ClassLoader(getClass().getClassLoader()) {
//            @Override
//            protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
//                if (name.equals(generatedClassName)) {
//                    synchronized (this) {
//                        Class<?> loadedClass = findLoadedClass(name);
//                        if (loadedClass != null) {
//                            return loadedClass;
//                        }
//                        return defineClass(generatedClassName, aClass, 0, aClass.length);
//                    }
//                }
//                return super.loadClass(name, resolve);
//            }
//        };
//
//        Class<?> generatedClass = classLoader.loadClass(generatedClassName);
//        System.out.println(generatedClass);
//
//        Function<Integer, String> instance = (Function<Integer, String>) generatedClass.newInstance();
//        String result = instance.apply(1);
//        System.out.println("Result: " + result);
//
//
//        inspectObject(instance);
//
//    }
//
//    private String translateLambdaIntoMethodName(Serializable o) {
//        SerializedLambda serializedLambda = Utils.toSerializedLambda(o);
//        String implMethodName = serializedLambda.getImplMethodName();
//        System.out.println("Impl Signature: " + serializedLambda.getImplMethodSignature());
//        System.out.println("Interface Signature: " + serializedLambda.getFunctionalInterfaceMethodSignature());
//        return implMethodName;
//    }
//
//}