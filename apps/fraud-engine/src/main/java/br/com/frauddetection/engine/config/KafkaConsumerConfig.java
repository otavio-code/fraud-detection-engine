package br.com.frauddetection.engine.config;

import br.com.frauddetection.engine.observability.FraudMetrics;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Qualifier;
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
            @Qualifier("dltKafkaTemplate")
            KafkaTemplate<Object, Object> kafkaTemplate,
            FraudMetrics fraudMetrics
    ) {

        DeadLetterPublishingRecoverer recoverer =
                new DeadLetterPublishingRecoverer(
                        kafkaTemplate,
                        (record, exception) -> {

                            fraudMetrics.registrarEnvioDlt();

                            return new TopicPartition(
                                    record.topic() + ".DLT",
                                    record.partition()
                            );
                        }
                );

        FixedBackOff fixedBackOff =
                new FixedBackOff(
                        2000L,
                        3L
                );

        return new DefaultErrorHandler(
                recoverer,
                fixedBackOff
        );
    }
}