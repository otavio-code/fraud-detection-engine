package br.com.frauddetection.engine.config;

import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConsumerConfig {
    @Bean
    public DefaultErrorHandler kafkaErrorHandler(
            KafkaTemplate<Object, Object> KafkaTemplate){
        DeadLetterPublishingRecoverer recoverer =
                new DeadLetterPublishingRecoverer(
                        KafkaTemplate,
                        (record, exception) ->
                                new TopicPartition(
                                        record.topic() + ".DLT",
                                        record.partition()
                                )
                );
        FixedBackOff fixedBackOff = new FixedBackOff(
                2000L,
                3L
        );
        return new DefaultErrorHandler(
                recoverer,
                fixedBackOff
        );
    }
}