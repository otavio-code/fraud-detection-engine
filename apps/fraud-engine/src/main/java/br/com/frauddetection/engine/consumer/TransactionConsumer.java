package br.com.frauddetection.engine.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class TransactionConsumer {
/*
    @KafkaListener(
            topics = "transactions.v1",
            groupId = "fraud-engine"
    )
    public void listener(ConsumerRecord<String, String> record){
        System.out.println("Tópico: " + record.topic());
        System.out.println("Partição: " + record.partition());
        System.out.println("Offset: " + record.offset());
        System.out.println("Chave: " + record.key());
        System.out.println("Mensagem: " + record.value());
        System.out.println(record.headers());
        Instant instant = Instant.ofEpochMilli(record.timestamp());
        System.out.println(instant);
    }
 */
@KafkaListener(
        topics = "transactions.v1",
        groupId = "fraud-engine"
)
public void consume(ConsumerRecord<String, String> record) {

    System.out.println("Tópico: " + record.topic());
    System.out.println("Partição: " + record.partition());
    System.out.println("Offset: " + record.offset());
    System.out.println("Chave: " + record.key());
    System.out.println("Mensagem: " + record.value());

    if (record.value().contains("\"id_transacao\":\"tx-fail\"")) {
        System.out.println("Simulando falha no processamento...");
        throw new RuntimeException("Falha proposital");
    }

    System.out.println("Processamento concluído");
}
}
