package br.com.frauddetection.engine.consumer;

import br.com.frauddetection.engine.alert.FraudAlertService;
import br.com.frauddetection.engine.idempotency.IdempotencyService;
import br.com.frauddetection.engine.idempotency.IdempotencyStatus;
import br.com.frauddetection.engine.observability.FraudMetrics;
import br.com.frauddetection.engine.rules.FraudRuleEngine;
import br.com.frauddetection.engine.rules.FraudRuleResult;
import br.com.frauddetection.events.TransactionEvent;
import io.micrometer.core.instrument.Timer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TransactionConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(TransactionConsumer.class);

    private final IdempotencyService idempotencyService;
    private final FraudRuleEngine fraudRuleEngine;
    private final FraudMetrics fraudMetrics;
    private final FraudAlertService fraudAlertService;

    public TransactionConsumer(
            IdempotencyService idempotencyService,
            FraudRuleEngine fraudRuleEngine,
            FraudMetrics fraudMetrics,
            FraudAlertService fraudAlertService
    ) {
        this.idempotencyService = idempotencyService;
        this.fraudRuleEngine = fraudRuleEngine;
        this.fraudMetrics = fraudMetrics;
        this.fraudAlertService = fraudAlertService;
    }

    @KafkaListener(
            topics = "${fraud.kafka.transaction-topic}",
            groupId = "${fraud.kafka.consumer-group}"
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

            log.info(
                    "Evento já processado. Ignorando. idEvento={}",
                    event.getIdEvento()
            );

            return;
        }

        if (status == IdempotencyStatus.PROCESSANDO) {

            log.warn(
                    "Evento já está sendo processado. idEvento={}",
                    event.getIdEvento()
            );

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

            log.error(
                    "Erro ao processar evento. idEvento={} topic={} partition={} offset={}",
                    event.getIdEvento(),
                    record.topic(),
                    record.partition(),
                    record.offset(),
                    exception
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

        log.info(
                "Evento Kafka recebido. topic={} partition={} offset={} key={} idEvento={} idTransacao={}",
                record.topic(),
                record.partition(),
                record.offset(),
                record.key(),
                event.getIdEvento(),
                event.getIdTransacao()
        );

        List<FraudRuleResult> resultados =
                fraudRuleEngine.evaluate(
                        event
                );

        if (resultados.isEmpty()) {

            log.info(
                    "Nenhuma suspeita detectada. idEvento={} idTransacao={}",
                    event.getIdEvento(),
                    event.getIdTransacao()
            );

            return;
        }

        fraudMetrics.registrarTransacaoSuspeita();

        fraudAlertService.enviarAlertas(
                event,
                resultados
        );

        resultados.forEach(resultado -> {

            fraudMetrics.registrarRegraSuspeita(
                    resultado.regra()
            );

            log.warn(
                    "Transação suspeita detectada. idEvento={} idTransacao={} regra={} motivo={}",
                    event.getIdEvento(),
                    event.getIdTransacao(),
                    resultado.regra(),
                    resultado.motivo()
            );
        });
    }
}