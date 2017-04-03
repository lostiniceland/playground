package osgi.eventhandler.provider.kafka;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "Kafka Producer",
        description = "Configures the producer for Kafka messages")
@interface KafkaConfiguration {

    @AttributeDefinition(description = "The name for this actor-system (used as OSGi-service property)")
    String bootstrapServer() default "localhost:9092";

    @AttributeDefinition(description = "Serializer used for messages")
    String serializer() default "org.apache.kafka.common.serialization.StringSerializer";

    @AttributeDefinition(description = "De-Serializer used for messages")
    String deserializer() default "org.apache.kafka.common.serialization.StringDeserializer";
}
