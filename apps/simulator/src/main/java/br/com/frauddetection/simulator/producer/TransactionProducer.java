package br.com.frauddetection.simulator.producer;

import br.com.frauddetection.events.TransactionEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class TransactionProducer {

    private static final String TOPIC = "transactions.v1";
    private final KafkaTemplate<String, TransactionEvent> kafkaTemplate;

    public TransactionProducer(
            KafkaTemplate<String, TransactionEvent> kafkaTemplate
    ) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(String key, TransactionEvent event) {
        kafkaTemplate.send(TOPIC, key, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        System.err.println("Erro ao publicar transação: " + ex.getMessage());
                        return;
                    }

                    System.out.printf(
                            "Transação publicada. topic=%s partition=%d offset=%d%n",
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset()
                    );
                });
    }
}