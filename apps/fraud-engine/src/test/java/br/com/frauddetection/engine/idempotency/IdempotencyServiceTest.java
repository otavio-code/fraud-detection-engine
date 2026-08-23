package br.com.frauddetection.engine.idempotency;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class IdempotencyServiceTest {
    @Test
    void deveRetornarAdquiridoQuandoEventoNaoExistirNoRedis() {

        // Arrange
        StringRedisTemplate redisTemplate =
                Mockito.mock(StringRedisTemplate.class);

        ValueOperations<String, String> valueOperations =
                Mockito.mock(ValueOperations.class);

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        when(
                valueOperations.setIfAbsent(
                        Mockito.anyString(),
                        Mockito.eq("PROCESSANDO"),
                        Mockito.any(Duration.class)
                )
        ).thenReturn(true);

        IdempotencyService service =
                new IdempotencyService(redisTemplate);

        // Act
        IdempotencyStatus resultado =
                service.tryAcquire("001");

        // Assert
        assertEquals(
                IdempotencyStatus.ADQUIRIDO,
                resultado
        );
    }

    @Test
    void deveRetornarProcessadoQuandoEventoJaTiverSidoProcessado() {

        // Arrange
        StringRedisTemplate redisTemplate =
                Mockito.mock(StringRedisTemplate.class);

        ValueOperations<String, String> valueOperations =
                Mockito.mock(ValueOperations.class);

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        when(
                valueOperations.setIfAbsent(
                        Mockito.anyString(),
                        Mockito.eq("PROCESSANDO"),
                        Mockito.any(Duration.class)
                )
        ).thenReturn(false);

        when(
                valueOperations.get(
                        "fraud:idempotency:event:002"
                )
        ).thenReturn(
                "PROCESSADO"
        );

        IdempotencyService service =
                new IdempotencyService(redisTemplate);

        // Act
        IdempotencyStatus resultado =
                service.tryAcquire("002");

        // Assert
        assertEquals(
                IdempotencyStatus.PROCESSADO,
                resultado
        );
    }

    @Test
    void deveRetornarProcessandoQuandoEventoJaEstiverSendoProcessado() {
        // Arrange
        StringRedisTemplate redisTemplate =
                Mockito.mock(StringRedisTemplate.class);

        ValueOperations<String, String> valueOperations =
                Mockito.mock(ValueOperations.class);

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        when(
                valueOperations.setIfAbsent(
                        Mockito.anyString(),
                        Mockito.eq("PROCESSANDO"),
                        Mockito.any(Duration.class)
                )
        ).thenReturn(false);

        when(
                valueOperations.get(
                        "fraud:idempotency:event:003"
                )
        ).thenReturn(
                "PROCESSANDO"
        );

        IdempotencyService service =
                new IdempotencyService(redisTemplate);

        // Act
        IdempotencyStatus resultado =
                service.tryAcquire("003");

        // Assert
        assertEquals(
                IdempotencyStatus.PROCESSANDO,
                resultado
        );


    }
}
