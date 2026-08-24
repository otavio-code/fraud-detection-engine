package br.com.frauddetection.engine.alert;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

class EmailCustomerNotificationServiceTest {

    @Test
    void deveEnviarEmailDeAlertaAoCliente() {

        // Arrange
        JavaMailSender mailSender =
                Mockito.mock(JavaMailSender.class);

        EmailCustomerNotificationService service =
                new EmailCustomerNotificationService(
                        mailSender,
                        "fraud-engine@localhost",
                        "cliente@localhost"
                );

        FraudAlert alert =
                new FraudAlert(
                        "alerta-001",
                        "evento-001",
                        "transacao-001",
                        "cliente-001",
                        List.of(
                                "TRANSACAO_VALOR_ALTO",
                                "MUITAS_TRANSACOES_CURTO_PERIODO"
                        ),
                        Instant.parse(
                                "2026-08-24T11:00:00Z"
                        )
                );

        ArgumentCaptor<SimpleMailMessage> captor =
                ArgumentCaptor.forClass(
                        SimpleMailMessage.class
                );

        // Act
        service.notify(alert);

        // Assert
        verify(mailSender)
                .send(captor.capture());

        SimpleMailMessage message =
                captor.getValue();

        assertEquals(
                "fraud-engine@localhost",
                message.getFrom()
        );

        assertNotNull(
                message.getTo()
        );

        assertEquals(
                "cliente@localhost",
                message.getTo()[0]
        );

        assertEquals(
                "Alerta de transação suspeita",
                message.getSubject()
        );

        assertNotNull(
                message.getText()
        );

        assertTrue(
                message.getText()
                        .contains("alerta-001")
        );

        assertTrue(
                message.getText()
                        .contains("evento-001")
        );

        assertTrue(
                message.getText()
                        .contains("transacao-001")
        );

        assertTrue(
                message.getText()
                        .contains("cliente-001")
        );

        assertTrue(
                message.getText()
                        .contains(
                                "TRANSACAO_VALOR_ALTO"
                        )
        );

        assertTrue(
                message.getText()
                        .contains(
                                "MUITAS_TRANSACOES_CURTO_PERIODO"
                        )
        );
    }
}