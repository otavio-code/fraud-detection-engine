package br.com.frauddetection.engine.controller;

import br.com.frauddetection.engine.configuration.ConfiguracaoRegraService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;

class RegraFraudeAdminControllerTest {

    @Test
    void deveAtualizarLimiteDaRegraDeTransacaoValorAlto() {

        // Arrange
        ConfiguracaoRegraService configuracaoRegraService =
                Mockito.mock(ConfiguracaoRegraService.class);

        RegraFraudeAdminController controller =
                new RegraFraudeAdminController(
                        configuracaoRegraService
                );

        UpdateLimitRequest request =
                new UpdateLimitRequest(
                        new BigDecimal("20000.00")
                );

        // Act
        controller.atualizarLimite(request);

        // Assert
        Mockito.verify(configuracaoRegraService)
                .atualizarLimiteTransacaoValorAlto(
                        new BigDecimal("20000.00")
                );
    }
}