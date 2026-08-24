package br.com.frauddetection.engine.alert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaInternalAlertPublisher
        implements InternalAlertPublisher {

    private static final Logger log =
            LoggerFactory.getLogger(
                    KafkaInternalAlertPublisher.class
            );

    private final KafkaTemplate<String, FraudAlert> kafkaTemplate;
    private final String topic;

    public KafkaInternalAlertPublisher(
            KafkaTemplate<String, FraudAlert> kafkaTemplate,
            @Value("${fraud.kafka.alert-topic}")
            String topic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @Override
    public void publish(FraudAlert alert) {

        kafkaTemplate
                .send(
                        topic,
                        alert.idCliente(),
                        alert
                )
                .whenComplete(
                        (result, exception) -> {

                            if (exception != null) {

                                log.error(
                                        "Erro ao publicar alerta interno. idAlerta={} idEvento={}",
                                        alert.idAlerta(),
                                        alert.idEvento(),
                                        exception
                                );

                                return;
                            }

                            log.info(
                                    "Alerta interno publicado. idAlerta={} topic={} partition={} offset={}",
                                    alert.idAlerta(),
                                    result.getRecordMetadata().topic(),
                                    result.getRecordMetadata().partition(),
                                    result.getRecordMetadata().offset()
                            );
                        }
                );
    }
}