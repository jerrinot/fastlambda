package info.jerrinot.fastlambda;

import com.hazelcast.nio.serialization.HazelcastSerializationException;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Utils {
    public static SerializedLambda toSerializedLambda(Object object) {
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
}
