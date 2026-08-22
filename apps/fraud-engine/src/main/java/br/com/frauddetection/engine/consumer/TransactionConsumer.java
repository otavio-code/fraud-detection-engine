package br.com.frauddetection.engine.consumer;

import br.com.frauddetection.events.TransactionEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TransactionConsumer {

    @KafkaListener(
            topics = "transactions.v1",
            groupId = "fraud-engine"
    )
    public void consume(ConsumerRecord<String, TransactionEvent> record) {

        TransactionEvent event = record.value();

        System.out.println("Evento Kafka recebido:");
        System.out.println("Tópico: " + record.topic());
        System.out.println("Partição: " + record.partition());
        System.out.println("Offset: " + record.offset());
        System.out.println("Chave: " + record.key());

        System.out.println("Transação:");
        System.out.println("idEvento: " + event.getIdEvento());
        System.out.println("idTransacao: " + event.getIdTransacao());
        System.out.println("idCliente: " + event.getIdCliente());
        System.out.println("contaOrigem: " + event.getIdContaOrigem());
        System.out.println("contaDestino: " + event.getIdContaDestino());
        System.out.println("valor: " + event.getValorTransacao());
        System.out.println("moeda: " + event.getCodigoMoeda());
        System.out.println("tipo: " + event.getTipoTransacao());
        System.out.println("dataHora: " + event.getDataHoraTransacao());
    }
}