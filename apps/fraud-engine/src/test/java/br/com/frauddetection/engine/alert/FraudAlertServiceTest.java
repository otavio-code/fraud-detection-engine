package br.com.frauddetection.engine.alert;

import br.com.frauddetection.engine.rules.FraudRuleResult;
import br.com.frauddetection.events.TransactionEvent;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FraudAlertServiceTest {

    @Test
    void deveEnviarAlertaParaCanalInternoEExterno() {

        // Arrange
        InternalAlertPublisher internalAlertPublisher =
                Mockito.mock(InternalAlertPublisher.class);

        CustomerNotificationService customerNotificationService =
                Mockito.mock(CustomerNotificationService.class);

        TransactionEvent event =
                Mockito.mock(TransactionEvent.class);

        when(event.getIdEvento())
                .thenReturn("evento-001");

        when(event.getIdTransacao())
                .thenReturn("transacao-001");

        when(event.getIdCliente())
                .thenReturn("cliente-001");

        List<FraudRuleResult> resultados =
                List.of(
                        new FraudRuleResult(
                                "TRANSACAO_VALOR_ALTO",
                                true,
                                "Transação acima do limite"
                        ),
                        new FraudRuleResult(
                                "TRANSACAO_HORARIO_INCOMUM",
                                true,
                                "Transação realizada em horário incomum"
                        )
                );

        FraudAlertService service =
                new FraudAlertService(
                        internalAlertPublisher,
                        customerNotificationService
                );

        ArgumentCaptor<FraudAlert> captor =
                ArgumentCaptor.forClass(FraudAlert.class);

        // Act
        service.enviarAlertas(
                event,
                resultados
        );

        // Assert
        verify(internalAlertPublisher)
                .publish(captor.capture());

        FraudAlert alert =
                captor.getValue();

        assertNotNull(alert.idAlerta());

        assertEquals(
                "evento-001",
                alert.idEvento()
        );

        assertEquals(
                "transacao-001",
                alert.idTransacao()
        );

        assertEquals(
                "cliente-001",
                alert.idCliente()
        );

        assertEquals(
                List.of(
                        "TRANSACAO_VALOR_ALTO",
                        "TRANSACAO_HORARIO_INCOMUM"
                ),
                alert.regras()
        );

        assertNotNull(alert.dataHora());

        verify(customerNotificationService)
                .notify(alert);
    }
}