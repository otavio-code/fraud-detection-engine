package br.com.frauddetection.engine.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import br.com.frauddetection.events.TransactionEvent;

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
    public void consume(TransactionEvent event) {

        System.out.println("Transação recebida:");
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
