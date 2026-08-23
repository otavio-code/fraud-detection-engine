package br.com.frauddetection.engine.configuration;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class ConfiguracaoRegraServiceTest {
    @Test
    void deveObterLimiteDoRedisQuandoConfigurado() {

        // Arrange
        StringRedisTemplate redisTemplate =
                Mockito.mock(StringRedisTemplate.class);

        ValueOperations<String, String> valueOperations =
                Mockito.mock(ValueOperations.class);

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        when(
                valueOperations.get(
                        "fraude:regras:transacao-valor-alto:limite"
                )
        ).thenReturn(
                "20000.00"
        );

        ConfiguracaoRegraService service =
                new ConfiguracaoRegraService(
                        redisTemplate,
                        new BigDecimal("10000.00")
                );

        // Act
        BigDecimal resultado =
                service.obterLimiteTransacaoValorAlto();

        // Assert
        assertEquals(
                new BigDecimal("20000.00"),
                resultado
        );
    }

    @Test
    void deveRetornarLimitePadraoQuandoRedisNaoPossuirConfiguracao(){

        // Arrange
        StringRedisTemplate redisTemplate =
                Mockito.mock(StringRedisTemplate.class);

        ValueOperations<String, String> valueOperations =
                Mockito.mock(ValueOperations.class);

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        when(
                valueOperations.get(
                        "fraude:regras:transacao-valor-alto:limite"
                )
        ).thenReturn(
                null
        );

        ConfiguracaoRegraService service =
                new ConfiguracaoRegraService(
                        redisTemplate,
                        new BigDecimal("10000.00")
                );

        // Act
        BigDecimal resultado =
                service.obterLimiteTransacaoValorAlto();

        // Assert
        assertEquals(
                new BigDecimal("10000.00"),
                resultado
        );
    }

    @Test
    void deveAtualizarLimiteNoRedis() {

        // Arrange
        StringRedisTemplate redisTemplate =
                Mockito.mock(StringRedisTemplate.class);

        ValueOperations<String, String> valueOperations =
                Mockito.mock(ValueOperations.class);

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        ConfiguracaoRegraService service =
                new ConfiguracaoRegraService(
                        redisTemplate,
                        new BigDecimal("10000.00")
                );

        // Act
        service.atualizarLimiteTransacaoValorAlto(
                new BigDecimal("25000.00")
        );

        // Assert
        Mockito.verify(valueOperations)
                .set(
                        "fraude:regras:transacao-valor-alto:limite",
                        "25000.00"
                );
    }

}
