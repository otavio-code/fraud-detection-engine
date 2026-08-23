package br.com.frauddetection.engine.consumer;

import br.com.frauddetection.engine.idempotency.IdempotencyService;
import br.com.frauddetection.engine.idempotency.IdempotencyStatus;
import br.com.frauddetection.engine.rules.FraudRuleEngine;
import br.com.frauddetection.events.TransactionEvent;
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

        //Arrange
        IdempotencyService idempotencyService =
                Mockito.mock(IdempotencyService.class);

        FraudRuleEngine fraudRuleEngine =
                Mockito.mock(FraudRuleEngine.class);

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
                        fraudRuleEngine
                );

        //Act
        consumer.consume(record);

        //Assert
        Mockito.verifyNoInteractions(fraudRuleEngine);

        Mockito.verify(
                idempotencyService,
                Mockito.never()
        ).markProcessed(Mockito.anyString());
    }

    @Test
    void deveLancarExcecaoQuandoEventoJaEstiverSendoProcessado() {

        // Arrange
        IdempotencyService idempotencyService =
                Mockito.mock(IdempotencyService.class);

        FraudRuleEngine fraudRuleEngine =
                Mockito.mock(FraudRuleEngine.class);

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
                        fraudRuleEngine
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

        Mockito.verify(
                idempotencyService,
                Mockito.never()
        ).markProcessed(Mockito.anyString());
    }

    @Test
    void deveProcessarEventoAdquiridoEMarcarComoProcessado() {

        // Arrange
        IdempotencyService idempotencyService =
                Mockito.mock(IdempotencyService.class);

        FraudRuleEngine fraudRuleEngine =
                Mockito.mock(FraudRuleEngine.class);

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

        TransactionConsumer consumer =
                new TransactionConsumer(
                        idempotencyService,
                        fraudRuleEngine
                );

        // Act
        consumer.consume(record);

        // Assert
        Mockito.verify(fraudRuleEngine)
                .evaluate(event);

        Mockito.verify(idempotencyService)
                .markProcessed("evento-003");

        Mockito.verify(
                idempotencyService,
                Mockito.never()
        ).release(Mockito.anyString());
    }

    @Test
    void deveLiberarEventoQuandoOcorrerErroNoProcessamento() {

        // Arrange
        IdempotencyService idempotencyService =
                Mockito.mock(IdempotencyService.class);

        FraudRuleEngine fraudRuleEngine =
                Mockito.mock(FraudRuleEngine.class);

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

        TransactionConsumer consumer =
                new TransactionConsumer(
                        idempotencyService,
                        fraudRuleEngine
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

        Mockito.verify(idempotencyService)
                .release("evento-004");

        Mockito.verify(
                idempotencyService,
                Mockito.never()
        ).markProcessed(Mockito.anyString());
    }
}