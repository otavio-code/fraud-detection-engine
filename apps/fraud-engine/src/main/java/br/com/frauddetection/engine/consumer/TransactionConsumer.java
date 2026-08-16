package br.com.frauddetection.engine.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TransactionConsumer {
    @KafkaListener(
            topics = "transactions.v1",
            groupId = "fraud-engine"
    )
    public void consumer(String message){
        System.out.println("Mensagem recebida: " + message);
    }
}
