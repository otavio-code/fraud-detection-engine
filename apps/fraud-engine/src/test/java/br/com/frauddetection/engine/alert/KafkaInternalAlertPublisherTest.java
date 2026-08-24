package br.com.frauddetection.engine.alert;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class KafkaInternalAlertPublisherTest {

    @Test
    void devePublicarAlertaNoTopicoKafka() {

        // Arrange
        KafkaTemplate<String, FraudAlert> kafkaTemplate =
                Mockito.mock(KafkaTemplate.class);

        FraudAlert alert =
                new FraudAlert(
                        "alerta-001",
                        "evento-001",
                        "transacao-001",
                        "cliente-001",
                        List.of("TRANSACAO_VALOR_ALTO"),
                        Instant.now()
                );

        SendResult<String, FraudAlert> sendResult =
                Mockito.mock(SendResult.class);

        CompletableFuture<SendResult<String, FraudAlert>> future =
                CompletableFuture.completedFuture(
                        sendResult
                );

        when(
                kafkaTemplate.send(
                        "fraud-alerts.v1",
                        "cliente-001",
                        alert
                )
        ).thenReturn(
                future
        );

        KafkaInternalAlertPublisher publisher =
                new KafkaInternalAlertPublisher(
                        kafkaTemplate,
                        "fraud-alerts.v1"
                );

        // Act
        publisher.publish(alert);

        // Assert
        verify(kafkaTemplate)
                .send(
                        "fraud-alerts.v1",
                        "cliente-001",
                        alert
                );
    }

    @Test
    void deveTratarErroAoPublicarAlertaNoKafka() {

        // Arrange
        KafkaTemplate<String, FraudAlert> kafkaTemplate =
                Mockito.mock(KafkaTemplate.class);

        FraudAlert alert =
                new FraudAlert(
                        "alerta-002",
                        "evento-002",
                        "transacao-002",
                        "cliente-002",
                        List.of("TRANSACAO_VALOR_ALTO"),
                        Instant.now()
                );

        CompletableFuture<SendResult<String, FraudAlert>> future =
                new CompletableFuture<>();

        future.completeExceptionally(
                new RuntimeException(
                        "Erro Kafka"
                )
        );

        when(
                kafkaTemplate.send(
                        "fraud-alerts.v1",
                        "cliente-002",
                        alert
                )
        ).thenReturn(
                future
        );

        KafkaInternalAlertPublisher publisher =
                new KafkaInternalAlertPublisher(
                        kafkaTemplate,
                        "fraud-alerts.v1"
                );

        // Act
        publisher.publish(alert);

        // Assert
        verify(kafkaTemplate)
                .send(
                        "fraud-alerts.v1",
                        "cliente-002",
                        alert
                );
    }
}