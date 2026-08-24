package br.com.frauddetection.engine.config;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class DltKafkaProducerConfig {

    @Bean
    public ProducerFactory<Object, Object> dltProducerFactory(
            @Value("${spring.kafka.bootstrap-servers}")
            String bootstrapServers,

            @Value("${spring.kafka.properties.schema.registry.url}")
            String schemaRegistryUrl
    ) {

        Map<String, Object> properties =
                new HashMap<>();

        properties.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers
        );

        properties.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class
        );

        properties.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                KafkaAvroSerializer.class
        );

        properties.put(
                "schema.registry.url",
                schemaRegistryUrl
        );

        return new DefaultKafkaProducerFactory<>(
                properties
        );
    }

    @Bean
    public KafkaTemplate<Object, Object> dltKafkaTemplate(
            ProducerFactory<Object, Object> dltProducerFactory
    ) {

        return new KafkaTemplate<>(
                dltProducerFactory
        );
    }
}