package br.com.frauddetection.engine.rules;

import br.com.frauddetection.events.TransactionEvent;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class TransactionVelocityRuleTest {

    @Test
    void deveConsiderarSuspeitaQuandoQuantidadeUltrapassarLimite() {

        // Arrange
        StringRedisTemplate redisTemplate =
                Mockito.mock(StringRedisTemplate.class);

        ValueOperations<String, String> valueOperations =
                Mockito.mock(ValueOperations.class);

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        when(
                valueOperations.increment(
                        "fraude:regras:velocidade:conta:123"
                )
        ).thenReturn(6L);

        TransactionEvent event =
                Mockito.mock(TransactionEvent.class);

        when(event.getIdContaOrigem())
                .thenReturn("123");

        TransactionVelocityRule rule =
                new TransactionVelocityRule(
                        redisTemplate,
                        5,
                        Duration.ofSeconds(60)
                );

        // Act
        FraudRuleResult resultado =
                rule.evaluate(event);

        // Assert
        assertTrue(resultado.suspeita());

        assertEquals(
                "MUITAS_TRANSACOES_CURTO_PERIODO",
                resultado.regra()
        );

        assertEquals(
                "Quantidade de transações acima do limite no período",
                resultado.motivo()
        );
    }
    @Test
    void deveConsiderarNaoSuspeitaQuandoQuantidadeForIgualAoLimite() {

        // Arrange
        StringRedisTemplate redisTemplate =
                Mockito.mock(StringRedisTemplate.class);

        ValueOperations<String, String> valueOperations =
                Mockito.mock(ValueOperations.class);

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        when(
                valueOperations.increment(
                        "fraude:regras:velocidade:conta:123"
                )
        ).thenReturn(5L);

        TransactionEvent event =
                Mockito.mock(TransactionEvent.class);

        when(event.getIdContaOrigem())
                .thenReturn("123");

        TransactionVelocityRule rule =
                new TransactionVelocityRule(
                        redisTemplate,
                        5,
                        Duration.ofSeconds(60)
                );

        // Act
        FraudRuleResult resultado =
                rule.evaluate(event);

        // Assert
        assertFalse(resultado.suspeita());

        assertEquals(
                "MUITAS_TRANSACOES_CURTO_PERIODO",
                resultado.regra()
        );

        assertNull(resultado.motivo());
    }

    @Test
    void deveConfigurarExpiracaoQuandoForPrimeiraTransacaoDaJanela() {

        // Arrange
        StringRedisTemplate redisTemplate =
                Mockito.mock(StringRedisTemplate.class);

        ValueOperations<String, String> valueOperations =
                Mockito.mock(ValueOperations.class);

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        when(
                valueOperations.increment(
                        "fraude:regras:velocidade:conta:123"
                )
        ).thenReturn(1L);

        TransactionEvent event =
                Mockito.mock(TransactionEvent.class);

        when(event.getIdContaOrigem())
                .thenReturn("123");

        TransactionVelocityRule rule =
                new TransactionVelocityRule(
                        redisTemplate,
                        5,
                        Duration.ofSeconds(60)
                );

        // Act
        FraudRuleResult resultado =
                rule.evaluate(event);

        // Assert
        assertFalse(resultado.suspeita());

        Mockito.verify(redisTemplate)
                .expire(
                        "fraude:regras:velocidade:conta:123",
                        Duration.ofSeconds(60)
                );
    }

    @Test
    void naoDeveRenovarExpiracaoQuandoNaoForPrimeiraTransacaoDaJanela() {

        // Arrange
        StringRedisTemplate redisTemplate =
                Mockito.mock(StringRedisTemplate.class);

        ValueOperations<String, String> valueOperations =
                Mockito.mock(ValueOperations.class);

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        when(
                valueOperations.increment(
                        "fraude:regras:velocidade:conta:123"
                )
        ).thenReturn(2L);

        TransactionEvent event =
                Mockito.mock(TransactionEvent.class);

        when(event.getIdContaOrigem())
                .thenReturn("123");

        TransactionVelocityRule rule =
                new TransactionVelocityRule(
                        redisTemplate,
                        5,
                        Duration.ofSeconds(60)
                );

        // Act
        FraudRuleResult resultado =
                rule.evaluate(event);

        // Assert
        assertFalse(resultado.suspeita());

        Mockito.verify(
                redisTemplate,
                Mockito.never()
        ).expire(
                Mockito.anyString(),
                Mockito.any(Duration.class)
        );
    }
}