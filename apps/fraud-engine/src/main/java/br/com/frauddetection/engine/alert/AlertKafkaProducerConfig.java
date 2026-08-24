package br.com.frauddetection.engine.alert;

import br.com.frauddetection.engine.alert.FraudAlert;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class AlertKafkaProducerConfig {

    @Bean
    public ProducerFactory<String, FraudAlert> alertProducerFactory(
            @Value("${spring.kafka.bootstrap-servers}")
            String bootstrapServers
    ) {

        Map<String, Object> properties =
                new HashMap<>();

        properties.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers
        );

        ObjectMapper objectMapper =
                new ObjectMapper();

        objectMapper.registerModule(
                new JavaTimeModule()
        );

        JsonSerializer<FraudAlert> jsonSerializer =
                new JsonSerializer<>(
                        objectMapper
                );

        return new DefaultKafkaProducerFactory<>(
                properties,
                new StringSerializer(),
                jsonSerializer
        );
    }

    @Bean
    public KafkaTemplate<String, FraudAlert> alertKafkaTemplate(
            ProducerFactory<String, FraudAlert> alertProducerFactory
    ) {

        return new KafkaTemplate<>(
                alertProducerFactory
        );
    }
}