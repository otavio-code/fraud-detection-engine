package br.com.frauddetection.engine.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConsumerConfig {
    @Bean
    public DefaultErrorHandler kafkaErrorHandler(){
        FixedBackOff fixedBackOff = new FixedBackOff(
                5000L,
                3L
        );
        return new DefaultErrorHandler(fixedBackOff);
    }
}
