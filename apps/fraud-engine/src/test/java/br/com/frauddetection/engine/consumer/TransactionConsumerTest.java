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
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class TransactionConsumerTest {

    @Test
    void deveIgnorarEventoQuandoJaEstiverProcessado() {

        // Arrange
        IdempotencyService idempotencyService =
                Mockito.mock(IdempotencyService.class);

        FraudRuleEngine fraudRuleEngine =
                Mockito.mock(FraudRuleEngine.class);

        FraudMetrics fraudMetrics =
                Mockito.mock(FraudMetrics.class);

        FraudAlertService fraudAlertService =
                Mockito.mock(FraudAlertService.class);

        TransactionEvent event =
                Mockito.mock(TransactionEvent.class);

        ConsumerRecord<String, TransactionEvent> record =
                Mockito.mock(ConsumerRecord.class);

        when(record.value())
                .thenReturn(event);

        when(event.getIdEvento())
                .thenReturn("001");

        when(
                idempotencyService.tryAcquire("001")
        ).thenReturn(
                IdempotencyStatus.PROCESSADO
        );

        TransactionConsumer consumer =
                new TransactionConsumer(
                        idempotencyService,
                        fraudRuleEngine,
                        fraudMetrics,
                        fraudAlertService
                );

        // Act
        consumer.consume(record);

        // Assert
        Mockito.verifyNoInteractions(fraudRuleEngine);
        Mockito.verifyNoInteractions(fraudAlertService);

        Mockito.verify(
                idempotencyService,
                Mockito.never()
        ).markProcessed(Mockito.anyString());

        Mockito.verify(
                fraudMetrics,
                Mockito.never()
        ).iniciarProcessamento();
    }

    @Test
    void deveLancarExcecaoQuandoEventoJaEstiverSendoProcessado() {

        // Arrange
        IdempotencyService idempotencyService =
                Mockito.mock(IdempotencyService.class);

        FraudRuleEngine fraudRuleEngine =
                Mockito.mock(FraudRuleEngine.class);

        FraudMetrics fraudMetrics =
                Mockito.mock(FraudMetrics.class);

        FraudAlertService fraudAlertService =
                Mockito.mock(FraudAlertService.class);

        TransactionEvent event =
                Mockito.mock(TransactionEvent.class);

        ConsumerRecord<String, TransactionEvent> record =
                Mockito.mock(ConsumerRecord.class);

        when(record.value())
                .thenReturn(event);

        when(event.getIdEvento())
                .thenReturn("evento-002");

        when(
                idempotencyService.tryAcquire("evento-002")
        ).thenReturn(
                IdempotencyStatus.PROCESSANDO
        );

        TransactionConsumer consumer =
                new TransactionConsumer(
                        idempotencyService,
                        fraudRuleEngine,
                        fraudMetrics,
                        fraudAlertService
                );

        // Act + Assert
        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> consumer.consume(record)
                );

        assertEquals(
                "Evento já está sendo processado: evento-002",
                exception.getMessage()
        );

        Mockito.verifyNoInteractions(fraudRuleEngine);
        Mockito.verifyNoInteractions(fraudAlertService);

        Mockito.verify(
                idempotencyService,
                Mockito.never()
        ).markProcessed(Mockito.anyString());

        Mockito.verify(
                fraudMetrics,
                Mockito.never()
        ).iniciarProcessamento();
    }

    @Test
    void deveProcessarEventoAdquiridoEMarcarComoProcessado() {

        // Arrange
        IdempotencyService idempotencyService =
                Mockito.mock(IdempotencyService.class);

        FraudRuleEngine fraudRuleEngine =
                Mockito.mock(FraudRuleEngine.class);

        FraudMetrics fraudMetrics =
                Mockito.mock(FraudMetrics.class);

        FraudAlertService fraudAlertService =
                Mockito.mock(FraudAlertService.class);

        Timer.Sample sample =
                Mockito.mock(Timer.Sample.class);

        TransactionEvent event =
                Mockito.mock(TransactionEvent.class);

        ConsumerRecord<String, TransactionEvent> record =
                Mockito.mock(ConsumerRecord.class);

        when(record.value())
                .thenReturn(event);

        when(event.getIdEvento())
                .thenReturn("evento-003");

        when(
                idempotencyService.tryAcquire("evento-003")
        ).thenReturn(
                IdempotencyStatus.ADQUIRIDO
        );

        when(
                fraudRuleEngine.evaluate(event)
        ).thenReturn(
                List.of()
        );

        when(
                fraudMetrics.iniciarProcessamento()
        ).thenReturn(
                sample
        );

        TransactionConsumer consumer =
                new TransactionConsumer(
                        idempotencyService,
                        fraudRuleEngine,
                        fraudMetrics,
                        fraudAlertService
                );

        // Act
        consumer.consume(record);

        // Assert
        Mockito.verify(fraudRuleEngine)
                .evaluate(event);

        Mockito.verifyNoInteractions(fraudAlertService);

        Mockito.verify(idempotencyService)
                .markProcessed("evento-003");

        Mockito.verify(
                idempotencyService,
                Mockito.never()
        ).release(Mockito.anyString());

        Mockito.verify(fraudMetrics)
                .registrarTransacaoProcessada();

        Mockito.verify(fraudMetrics)
                .finalizarProcessamento(sample);
    }

    @Test
    void deveLiberarEventoQuandoOcorrerErroNoProcessamento() {

        // Arrange
        IdempotencyService idempotencyService =
                Mockito.mock(IdempotencyService.class);

        FraudRuleEngine fraudRuleEngine =
                Mockito.mock(FraudRuleEngine.class);

        FraudMetrics fraudMetrics =
                Mockito.mock(FraudMetrics.class);

        FraudAlertService fraudAlertService =
                Mockito.mock(FraudAlertService.class);

        Timer.Sample sample =
                Mockito.mock(Timer.Sample.class);

        TransactionEvent event =
                Mockito.mock(TransactionEvent.class);

        ConsumerRecord<String, TransactionEvent> record =
                Mockito.mock(ConsumerRecord.class);

        when(record.value())
                .thenReturn(event);

        when(event.getIdEvento())
                .thenReturn("evento-004");

        when(
                idempotencyService.tryAcquire("evento-004")
        ).thenReturn(
                IdempotencyStatus.ADQUIRIDO
        );

        when(
                fraudRuleEngine.evaluate(event)
        ).thenThrow(
                new RuntimeException("Erro ao avaliar regras")
        );

        when(
                fraudMetrics.iniciarProcessamento()
        ).thenReturn(
                sample
        );

        TransactionConsumer consumer =
                new TransactionConsumer(
                        idempotencyService,
                        fraudRuleEngine,
                        fraudMetrics,
                        fraudAlertService
                );

        // Act + Assert
        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> consumer.consume(record)
                );

        assertEquals(
                "Erro ao avaliar regras",
                exception.getMessage()
        );

        Mockito.verifyNoInteractions(fraudAlertService);

        Mockito.verify(idempotencyService)
                .release("evento-004");

        Mockito.verify(
                idempotencyService,
                Mockito.never()
        ).markProcessed(Mockito.anyString());

        Mockito.verify(
                fraudMetrics,
                Mockito.never()
        ).registrarTransacaoProcessada();

        Mockito.verify(fraudMetrics)
                .finalizarProcessamento(sample);
    }

    @Test
    void deveRegistrarMetricasEEnviarAlertaQuandoTransacaoForSuspeita() {

        // Arrange
        IdempotencyService idempotencyService =
                Mockito.mock(IdempotencyService.class);

        FraudRuleEngine fraudRuleEngine =
                Mockito.mock(FraudRuleEngine.class);

        FraudMetrics fraudMetrics =
                Mockito.mock(FraudMetrics.class);

        FraudAlertService fraudAlertService =
                Mockito.mock(FraudAlertService.class);

        Timer.Sample sample =
                Mockito.mock(Timer.Sample.class);

        TransactionEvent event =
                Mockito.mock(TransactionEvent.class);

        ConsumerRecord<String, TransactionEvent> record =
                Mockito.mock(ConsumerRecord.class);

        when(record.value())
                .thenReturn(event);

        when(event.getIdEvento())
                .thenReturn("evento-005");

        when(
                idempotencyService.tryAcquire("evento-005")
        ).thenReturn(
                IdempotencyStatus.ADQUIRIDO
        );

        FraudRuleResult resultadoSuspeito =
                new FraudRuleResult(
                        "TRANSACAO_VALOR_ALTO",
                        true,
                        "Transação acima do limite"
                );

        List<FraudRuleResult> resultados =
                List.of(resultadoSuspeito);

        when(
                fraudRuleEngine.evaluate(event)
        ).thenReturn(
                resultados
        );

        when(
                fraudMetrics.iniciarProcessamento()
        ).thenReturn(
                sample
        );

        TransactionConsumer consumer =
                new TransactionConsumer(
                        idempotencyService,
                        fraudRuleEngine,
                        fraudMetrics,
                        fraudAlertService
                );

        // Act
        consumer.consume(record);

        // Assert
        Mockito.verify(fraudMetrics)
                .registrarTransacaoProcessada();

        Mockito.verify(fraudMetrics)
                .registrarTransacaoSuspeita();

        Mockito.verify(fraudMetrics)
                .registrarRegraSuspeita(
                        "TRANSACAO_VALOR_ALTO"
                );

        Mockito.verify(fraudAlertService)
                .enviarAlertas(
                        event,
                        resultados
                );

        Mockito.verify(fraudMetrics)
                .finalizarProcessamento(sample);

        Mockito.verify(idempotencyService)
                .markProcessed("evento-005");
    }

    @Test
    void deveEnviarUmAlertaEContabilizarTodasAsRegrasDisparadas() {

        // Arrange
        IdempotencyService idempotencyService =
                Mockito.mock(IdempotencyService.class);

        FraudRuleEngine fraudRuleEngine =
                Mockito.mock(FraudRuleEngine.class);

        FraudMetrics fraudMetrics =
                Mockito.mock(FraudMetrics.class);

        FraudAlertService fraudAlertService =
                Mockito.mock(FraudAlertService.class);

        Timer.Sample sample =
                Mockito.mock(Timer.Sample.class);

        TransactionEvent event =
                Mockito.mock(TransactionEvent.class);

        ConsumerRecord<String, TransactionEvent> record =
                Mockito.mock(ConsumerRecord.class);

        when(record.value())
                .thenReturn(event);

        when(event.getIdEvento())
                .thenReturn("evento-006");

        when(
                idempotencyService.tryAcquire("evento-006")
        ).thenReturn(
                IdempotencyStatus.ADQUIRIDO
        );

        FraudRuleResult regraValorAlto =
                new FraudRuleResult(
                        "TRANSACAO_VALOR_ALTO",
                        true,
                        "Transação acima do limite"
                );

        FraudRuleResult regraHorarioIncomum =
                new FraudRuleResult(
                        "TRANSACAO_HORARIO_INCOMUM",
                        true,
                        "Transação realizada em horário incomum"
                );

        List<FraudRuleResult> resultados =
                List.of(
                        regraValorAlto,
                        regraHorarioIncomum
                );

        when(
                fraudRuleEngine.evaluate(event)
        ).thenReturn(
                resultados
        );

        when(
                fraudMetrics.iniciarProcessamento()
        ).thenReturn(
                sample
        );

        TransactionConsumer consumer =
                new TransactionConsumer(
                        idempotencyService,
                        fraudRuleEngine,
                        fraudMetrics,
                        fraudAlertService
                );

        // Act
        consumer.consume(record);

        // Assert
        Mockito.verify(
                fraudMetrics,
                Mockito.times(1)
        ).registrarTransacaoSuspeita();

        Mockito.verify(fraudMetrics)
                .registrarRegraSuspeita(
                        "TRANSACAO_VALOR_ALTO"
                );

        Mockito.verify(fraudMetrics)
                .registrarRegraSuspeita(
                        "TRANSACAO_HORARIO_INCOMUM"
                );

        Mockito.verify(
                fraudAlertService,
                Mockito.times(1)
        ).enviarAlertas(
                event,
                resultados
        );

        Mockito.verify(
                fraudMetrics,
                Mockito.times(1)
        ).registrarTransacaoProcessada();

        Mockito.verify(fraudMetrics)
                .finalizarProcessamento(sample);

        Mockito.verify(idempotencyService)
                .markProcessed("evento-006");
    }
}