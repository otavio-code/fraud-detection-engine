package br.com.frauddetection.simulator;

import br.com.frauddetection.events.TransactionEvent;
import br.com.frauddetection.simulator.producer.TransactionProducer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;

@Component
@ConditionalOnProperty(
        name = "simulator.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class TransactionSimulatorRunner implements CommandLineRunner {

    private static final int QUANTIDADE_TRANSACOES = 6;

    private final TransactionProducer transactionProducer;

    public TransactionSimulatorRunner(
            TransactionProducer transactionProducer
    ) {
        this.transactionProducer = transactionProducer;
    }

    @Override
    public void run(String... args) {

        for (int i = 1; i <= QUANTIDADE_TRANSACOES; i++) {

            TransactionEvent event =
                    criarEvento(i);

            transactionProducer.send(
                    event.getIdContaOrigem(),
                    event
            );
        }
    }

    private TransactionEvent criarEvento(int numero) {

        return TransactionEvent.newBuilder()
                .setIdEvento("evento-123" + numero)
                .setIdTransacao("transacao-" + numero)
                .setIdCliente("cliente-001")
                .setIdContaOrigem("123")
                .setIdContaDestino("456")
                .setValorTransacao(
                        new BigDecimal("100000.00")
                )
                .setCodigoMoeda("BRL")
                .setTipoTransacao("PIX")
                .setDataHoraTransacao(
                        Instant.now()
                )
                .build();
    }
}