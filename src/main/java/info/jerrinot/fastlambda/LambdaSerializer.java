package info.jerrinot.fastlambda;

import com.hazelcast.internal.serialization.impl.JavaDefaultSerializers;
import com.hazelcast.nio.ClassLoaderUtil;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.HazelcastSerializationException;
import com.hazelcast.nio.serialization.StreamSerializer;
import org.xerial.snappy.SnappyOutputStream;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.xerial.snappy.Snappy.compress;
import static org.xerial.snappy.Snappy.uncompressString;

public class LambdaSerializer implements StreamSerializer {
    public static final int TYPE_ID = 1;

    private JavaDefaultSerializers.JavaSerializer javaSerializer;

    public LambdaSerializer() {
        this.javaSerializer = new JavaDefaultSerializers.JavaSerializer(false, false);
    }

    public void write(ObjectDataOutput out, Object object) throws IOException {
        checkSerializable(object);
        if (isLambda(object)) {
            out.writeBoolean(true);
            SnappyOutputStream snappyFramedOutputStream = new SnappyOutputStream((OutputStream) out);
            serializeLambda(out, object);
        } else {
            out.writeBoolean(false);
            javaSerializer.write(out, object);
        }
    }

    private void serializeLambda(ObjectDataOutput out, Object object) throws IOException {
        SerializedLambda serializedLambda = toSerializedLambda(object);
        String capturingClass = serializedLambda.getCapturingClass();
        out.writeByteArray(compress(capturingClass));
        String functionalInterfaceClass = serializedLambda.getFunctionalInterfaceClass();
        out.writeByteArray(compress(functionalInterfaceClass));
        String functionalInterfaceMethodName = serializedLambda.getFunctionalInterfaceMethodName();
        out.writeByteArray(compress(functionalInterfaceMethodName));
        String functionalInterfaceMethodSignature = serializedLambda.getFunctionalInterfaceMethodSignature();
        out.writeByteArray(compress(functionalInterfaceMethodSignature));
        int implMethodKind = serializedLambda.getImplMethodKind();
        out.writeInt(implMethodKind);
        String implClass = serializedLambda.getImplClass();
        out.writeByteArray(compress(implClass));
        String implMethodName = serializedLambda.getImplMethodName();
        out.writeByteArray(compress(implMethodName));
        String implMethodSignature = serializedLambda.getImplMethodSignature();
        out.writeByteArray(compress(implMethodSignature));
        String instantiatedMethodType = serializedLambda.getInstantiatedMethodType();
        out.writeByteArray(compress(instantiatedMethodType));
        int capturedArgCount = serializedLambda.getCapturedArgCount();
        out.writeInt(capturedArgCount);
        for (int i = 0; i < capturedArgCount; i++) {
            Object capturedArg = serializedLambda.getCapturedArg(i);
            out.writeObject(capturedArg);
        }
    }

    private SerializedLambda toSerializedLambda(Object object) {
        try {
            Method writeReplaceMethod = object.getClass().getDeclaredMethod("writeReplace");
            writeReplaceMethod.setAccessible(true);
            return  (SerializedLambda) writeReplaceMethod.invoke(object);
        } catch (NoSuchMethodException e) {
            throw new HazelcastSerializationException("Cannot serialize lambda " + object, e);
        } catch (IllegalAccessException e) {
            throw new HazelcastSerializationException("Cannot serialize lambda " + object, e);
        } catch (InvocationTargetException e) {
            throw new HazelcastSerializationException("Cannot serialize lambda " + object, e);
        }
    }

    private static void checkSerializable(Object object) {
        if (!(object instanceof Serializable)) {
            throw new HazelcastSerializationException(object.getClass().getName() + " does not implement " + Serializable.class.getName());
        }
    }

    public static boolean isLambda(Object object) {
        return object.getClass().getName().contains("$Lambda$");
    }

    public Object read(ObjectDataInput in) throws IOException {
        boolean isLambda = in.readBoolean();
        if (isLambda) {
            return deserializeLambda(in);
        }
        return javaSerializer.read(in);
    }

    private Object deserializeLambda(ObjectDataInput in) throws IOException {
        Class<?> capturingClass;
        String capturingClassName = uncompressString(in.readByteArray());
        capturingClassName = capturingClassName.replace('/', '.');
        ClassLoader classLoader = in.getClassLoader();
        try {
            capturingClass = ClassLoaderUtil.loadClass(classLoader, capturingClassName);
        } catch (ClassNotFoundException e) {
            throw new HazelcastSerializationException("Error while deserializing lambda", e);
        }
        String functionalInterfaceClass = uncompressString(in.readByteArray());
        String functionalInterfaceMethodName =  uncompressString(in.readByteArray());
        String functionalInterfaceMethodSignature = uncompressString(in.readByteArray());
        int implMethodKind = in.readInt();
        String implClass = uncompressString(in.readByteArray());
        String implMethodName = uncompressString(in.readByteArray());
        String implMethodSignature = uncompressString(in.readByteArray());
        String instantiatedMethodType = uncompressString(in.readByteArray());

        int capturedArgCount = in.readInt();
        Object[] capturedArgs = new Object[capturedArgCount];
        for (int i = 0; i < capturedArgCount; i++) {
            capturedArgs[i] = in.readObject();
        }

        SerializedLambda serializedLambda = new SerializedLambda(capturingClass, functionalInterfaceClass, functionalInterfaceMethodName,
                functionalInterfaceMethodSignature, implMethodKind, implClass, implMethodName, implMethodSignature,
                instantiatedMethodType, capturedArgs);

        try {
            Method readResolveMethod = SerializedLambda.class.getDeclaredMethod("readResolve");
            readResolveMethod.setAccessible(true);
            return readResolveMethod.invoke(serializedLambda);
        } catch (NoSuchMethodException e) {
            throw new HazelcastSerializationException("Error while deserializing lambda", e);
        } catch (IllegalAccessException e) {
            throw new HazelcastSerializationException("Error while deserializing lambda", e);
        } catch (InvocationTargetException e) {
            throw new HazelcastSerializationException("Error while deserializing lambda", e);
        }
    }

    public int getTypeId() {
        return TYPE_ID;
    }

    public void destroy() {
        javaSerializer.destroy();
    }
}
