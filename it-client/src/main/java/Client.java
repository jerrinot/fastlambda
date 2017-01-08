import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.GlobalSerializerConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.IMap;
import info.jerrinot.fastlambda.LambdaSerializer;

import java.io.Serializable;

public class Client {
    public static void main(String[] args) {
        ClientConfig config = new ClientConfig();

        config.getSerializationConfig().setGlobalSerializerConfig(
                new GlobalSerializerConfig()
                        .setOverrideJavaSerialization(true)
                        .setClassName(LambdaSerializer.class.getName()));

        HazelcastInstance client = HazelcastClient.newHazelcastClient(config);

        IExecutorService executorService = client.getExecutorService("executorService");
        executorService.submit((Runnable & Serializable) () -> System.out.println("Running 1st lambda"));

        IMap<Integer, Integer> map = client.getMap("myMap");

        client.shutdown();
    }
}
