import com.hazelcast.config.Config;
import com.hazelcast.config.GlobalSerializerConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import info.jerrinot.fastlambda.LambdaSerializer;

public class Member {
    public static void main(String[] args) {
        Config config = new Config();

        config.getSerializationConfig().setGlobalSerializerConfig(
                new GlobalSerializerConfig()
                        .setOverrideJavaSerialization(true)
                        .setClassName(LambdaSerializer.class.getName()));

        HazelcastInstance instance1 = Hazelcast.newHazelcastInstance(config);
    }
}
