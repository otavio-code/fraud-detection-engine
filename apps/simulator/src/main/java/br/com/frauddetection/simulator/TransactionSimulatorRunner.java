package br.com.frauddetection.simulator;

import br.com.frauddetection.events.TransactionEvent;
import br.com.frauddetection.simulator.producer.TransactionProducer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;

@Component
public class TransactionSimulatorRunner implements CommandLineRunner {

    private final TransactionProducer transactionProducer;

    public TransactionSimulatorRunner(TransactionProducer transactionProducer) {
        this.transactionProducer = transactionProducer;
    }

    @Override
    public void run(String... args) {

        for (int i = 1; i <= 6; i++) {

            TransactionEvent event = TransactionEvent.newBuilder()
                    .setIdEvento("evento-velocidade-" + i)
                    .setIdTransacao("transacao-velocidade-" + i)
                    .setIdCliente("cliente-001")
                    .setIdContaOrigem("123")
                    .setIdContaDestino("456")
                    .setValorTransacao(new BigDecimal("100.00"))
                    .setCodigoMoeda("BRL")
                    .setTipoTransacao("PIX")
                    .setDataHoraTransacao(Instant.now())
                    .build();

            transactionProducer.send(
                    event.getIdContaOrigem(),
                    event
            );

            System.out.println(
                    "Evento publicado: " + event.getIdEvento()
            );
        }
    }
}