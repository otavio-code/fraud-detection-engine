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
        TransactionEvent event = TransactionEvent.newBuilder()
                .setIdEvento("98798791431")
                .setIdTransacao("123456789")
                .setIdCliente("001")
                .setIdContaOrigem("123")
                .setIdContaDestino("456")
                .setValorTransacao(new BigDecimal("15000.00"))
                .setCodigoMoeda("BRL")
                .setTipoTransacao("PIX")
                .setDataHoraTransacao(Instant.now())
                .build();

        transactionProducer.send("123", event);
    }
}