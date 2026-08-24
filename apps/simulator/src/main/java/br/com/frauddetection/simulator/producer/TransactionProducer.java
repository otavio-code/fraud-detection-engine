package br.com.frauddetection.simulator.producer;

import br.com.frauddetection.events.TransactionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class TransactionProducer {

    private static final Logger log =
            LoggerFactory.getLogger(TransactionProducer.class);

    private final KafkaTemplate<String, TransactionEvent> kafkaTemplate;
    private final String topic;

    public TransactionProducer(
            KafkaTemplate<String, TransactionEvent> kafkaTemplate,
            @Value("${fraud.kafka.transaction-topic}")
            String topic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void send(
            String key,
            TransactionEvent event
    ) {

        kafkaTemplate
                .send(
                        topic,
                        key,
                        event
                )
                .whenComplete(
                        (result, exception) -> {

                            if (exception != null) {

                                log.error(
                                        "Erro ao publicar transação. idEvento={} topic={} key={}",
                                        event.getIdEvento(),
                                        topic,
                                        key,
                                        exception
                                );

                                return;
                            }

                            log.info(
                                    "Transação publicada. idEvento={} topic={} partition={} offset={}",
                                    event.getIdEvento(),
                                    result.getRecordMetadata().topic(),
                                    result.getRecordMetadata().partition(),
                                    result.getRecordMetadata().offset()
                            );
                        }
                );
    }
}