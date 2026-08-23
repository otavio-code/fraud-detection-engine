package br.com.frauddetection.engine.rules;

import br.com.frauddetection.events.TransactionEvent;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class UnusualHourTransactionRuleTest {

    @Test
    void deveConsiderarTransacaoDeMadrugadaComoSuspeita() {

        // Arrange
        TransactionEvent event =
                Mockito.mock(TransactionEvent.class);

        /*
         * 06:00 UTC
         * =
         * 03:00 em America/Sao_Paulo
         */
        when(event.getDataHoraTransacao())
                .thenReturn(
                        Instant.parse("2026-08-23T06:00:00Z")
                );

        UnusualHourTransactionRule rule =
                new UnusualHourTransactionRule(
                        0,
                        5,
                        "America/Sao_Paulo"
                );

        // Act
        FraudRuleResult resultado =
                rule.evaluate(event);

        // Assert
        assertTrue(resultado.suspeita());

        assertEquals(
                "TRANSACAO_HORARIO_INCOMUM",
                resultado.regra()
        );

        assertEquals(
                "Transação realizada em horário incomum",
                resultado.motivo()
        );
    }

    @Test
    void deveConsiderarTransacaoDuranteODiaComoNaoSuspeita() {

        // Arrange
        TransactionEvent event =
                Mockito.mock(TransactionEvent.class);

        /*
         * 18:00 UTC
         * =
         * 15:00 em America/Sao_Paulo
         */
        when(event.getDataHoraTransacao())
                .thenReturn(
                        Instant.parse("2026-08-23T18:00:00Z")
                );

        UnusualHourTransactionRule rule =
                new UnusualHourTransactionRule(
                        0,
                        5,
                        "America/Sao_Paulo"
                );

        // Act
        FraudRuleResult resultado =
                rule.evaluate(event);

        // Assert
        assertFalse(resultado.suspeita());

        assertEquals(
                "TRANSACAO_HORARIO_INCOMUM",
                resultado.regra()
        );

        assertNull(resultado.motivo());
    }

    @Test
    void deveConsiderarHorarioInicialComoSuspeito() {

        // Arrange
        TransactionEvent event =
                Mockito.mock(TransactionEvent.class);

        /*
         * 03:00 UTC
         * =
         * 00:00 em America/Sao_Paulo
         */
        when(event.getDataHoraTransacao())
                .thenReturn(
                        Instant.parse("2026-08-23T03:00:00Z")
                );

        UnusualHourTransactionRule rule =
                new UnusualHourTransactionRule(
                        0,
                        5,
                        "America/Sao_Paulo"
                );

        // Act
        FraudRuleResult resultado =
                rule.evaluate(event);

        // Assert
        assertTrue(resultado.suspeita());
    }

    @Test
    void deveConsiderarHorarioFinalComoNaoSuspeito() {

        // Arrange
        TransactionEvent event =
                Mockito.mock(TransactionEvent.class);

        /*
         * 08:00 UTC
         * =
         * 05:00 em America/Sao_Paulo
         */
        when(event.getDataHoraTransacao())
                .thenReturn(
                        Instant.parse("2026-08-23T08:00:00Z")
                );

        UnusualHourTransactionRule rule =
                new UnusualHourTransactionRule(
                        0,
                        5,
                        "America/Sao_Paulo"
                );

        // Act
        FraudRuleResult resultado =
                rule.evaluate(event);

        // Assert
        assertFalse(resultado.suspeita());
    }
}