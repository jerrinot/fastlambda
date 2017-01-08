//package info.jerrinot.fastlambda;
//
//import com.hazelcast.config.Config;
//import com.hazelcast.config.GlobalSerializerConfig;
//import com.hazelcast.core.Hazelcast;
//import com.hazelcast.core.HazelcastInstance;
//import com.hazelcast.core.IExecutorService;
//import com.hazelcast.core.Member;
//import com.hazelcast.instance.Node;
//import com.hazelcast.internal.serialization.InternalSerializationService;
//import com.hazelcast.nio.serialization.Data;
//import com.hazelcast.test.HazelcastTestSupport;
//import org.junit.Test;
//
//import java.io.Serializable;
//import java.util.Map;
//import java.util.concurrent.Callable;
//import java.util.concurrent.ExecutionException;
//import java.util.concurrent.Future;
//import java.util.function.Function;
//
//import static org.junit.Assert.assertEquals;
//
//
//public class LambdaSerializerTest {
//
//    @Test
//    public void smokeTest() throws ExecutionException, InterruptedException {
//        Config config = new Config();
//        config.getSerializationConfig().setGlobalSerializerConfig(
//                new GlobalSerializerConfig()
//                        .setOverrideJavaSerialization(true)
//                        .setClassName(LambdaSerializer.class.getName()));
//
//        HazelcastInstance instance1 = Hazelcast.newHazelcastInstance(config);
//        HazelcastInstance instance2 = Hazelcast.newHazelcastInstance(config);
//
//        IExecutorService execService = instance1.getExecutorService("execService");
//        Map<Member, Future<String>> memberFutureMap = execService.submitToAllMembers((Callable<String> & Serializable) () -> "foo");
//        assertEquals(2, memberFutureMap.size());
//        for (Future<String> valueFuture : memberFutureMap.values()) {
//            String value = valueFuture.get();
//            assertEquals("foo", value);
//        }
//    }
//
//    @Test
//    public void sizeTest() {
//        Config config = new Config();
//        config.getSerializationConfig().setGlobalSerializerConfig(
//                new GlobalSerializerConfig()
//                        .setOverrideJavaSerialization(true)
//                        .setClassName(LambdaSerializer.class.getName()));
//
//        HazelcastInstance instance = Hazelcast.newHazelcastInstance(config);
//        Node node = HazelcastTestSupport.getNode(instance);
//        InternalSerializationService serializationService = node.getSerializationService();
//
//        int capturedDelta = 1;
//        Function c = (Function<Integer, Integer> & Serializable) (i) -> i + capturedDelta;
//        Data data = serializationService.toData(c);
//        int dataSize = data.dataSize();
//        System.out.println("Size: " + dataSize);
//    }
//}
