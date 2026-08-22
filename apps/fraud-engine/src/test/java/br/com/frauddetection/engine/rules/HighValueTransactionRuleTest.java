package br.com.frauddetection.engine.rules;

import br.com.frauddetection.engine.configuration.ConfiguracaoRegraService;
import br.com.frauddetection.events.TransactionEvent;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.swing.*;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.Mockito.when;

class HighValueTransactionRuleTest {
    @Test
    void deveIdentificarTransacaoAcimaDoLimiteComoSuspeita(){
        // Arrange
        ConfiguracaoRegraService configuracaoRegraService = Mockito.mock(ConfiguracaoRegraService.class);
        when(configuracaoRegraService.obterLimiteTransacaoValorAlto()
        ).thenReturn(
                new BigDecimal("10000.00")
        );

        HighValueTransactionRule rule = new HighValueTransactionRule(
                configuracaoRegraService
        );
        TransactionEvent event = TransactionEvent.newBuilder()
                .setIdEvento("123456789")
                .setIdTransacao("456")
                .setIdCliente("abc")
                .setIdContaOrigem("02646")
                .setIdContaDestino("02548")
                .setValorTransacao(new BigDecimal("15000.00"))
                .setCodigoMoeda("BRL")
                .setTipoTransacao("PIX")
                .setDataHoraTransacao(Instant.now())
                .build();



        // executar a regra
    }
}
