package br.com.frauddetection.engine.consumer;

import br.com.frauddetection.engine.idempotency.IdempotencyService;
import br.com.frauddetection.engine.idempotency.IdempotencyStatus;
import br.com.frauddetection.engine.observability.FraudMetrics;
import br.com.frauddetection.engine.rules.FraudRuleEngine;
import br.com.frauddetection.engine.rules.FraudRuleResult;
import br.com.frauddetection.events.TransactionEvent;
import io.micrometer.core.instrument.Timer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TransactionConsumer {

    private final IdempotencyService idempotencyService;
    private final FraudRuleEngine fraudRuleEngine;
    private final FraudMetrics fraudMetrics;

    public TransactionConsumer(
            IdempotencyService idempotencyService,
            FraudRuleEngine fraudRuleEngine,
            FraudMetrics fraudMetrics
    ) {
        this.idempotencyService = idempotencyService;
        this.fraudRuleEngine = fraudRuleEngine;
        this.fraudMetrics = fraudMetrics;
    }

    @KafkaListener(
            topics = "transactions.v1",
            groupId = "fraud-engine"
    )
    public void consume(
            ConsumerRecord<String, TransactionEvent> record
    ) {

        TransactionEvent event = record.value();

        IdempotencyStatus status =
                idempotencyService.tryAcquire(
                        event.getIdEvento()
                );

        if (status == IdempotencyStatus.PROCESSADO) {

            System.out.println(
                    "Evento já processado. Ignorando: "
                            + event.getIdEvento()
            );

            return;
        }

        if (status == IdempotencyStatus.PROCESSANDO) {

            throw new IllegalStateException(
                    "Evento já está sendo processado: "
                            + event.getIdEvento()
            );
        }

        Timer.Sample sample =
                fraudMetrics.iniciarProcessamento();

        try {

            process(
                    record,
                    event
            );

            fraudMetrics.registrarTransacaoProcessada();

            idempotencyService.markProcessed(
                    event.getIdEvento()
            );

        } catch (Exception exception) {

            idempotencyService.release(
                    event.getIdEvento()
            );

            throw exception;

        } finally {

            fraudMetrics.finalizarProcessamento(
                    sample
            );
        }
    }

    private void process(
            ConsumerRecord<String, TransactionEvent> record,
            TransactionEvent event
    ) {

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

        List<FraudRuleResult> resultados =
                fraudRuleEngine.evaluate(
                        event
                );

        if (resultados.isEmpty()) {

            System.out.println(
                    "Nenhuma suspeita detectada."
            );

            return;
        }

        fraudMetrics.registrarTransacaoSuspeita();

        System.out.println(
                "Transação suspeita detectada:"
        );

        resultados.forEach(resultado -> {

            fraudMetrics.registrarRegraSuspeita(
                    resultado.regra()
            );

            System.out.println(
                    "Regra: "
                            + resultado.regra()
            );

            System.out.println(
                    "Motivo: "
                            + resultado.motivo()
            );
        });
    }
}