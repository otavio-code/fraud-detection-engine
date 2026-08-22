package br.com.frauddetection.engine.idempotency;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class IdempotencyService {

    private static final Duration PROCESSING_TTL = Duration.ofMinutes(2);
    private static final Duration PROCESSED_TTL = Duration.ofHours(24);

    private final StringRedisTemplate redisTemplate;

    public IdempotencyService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public IdempotencyStatus tryAcquire(String eventId) {

        String key = buildKey(eventId);

        Boolean acquired = redisTemplate
                .opsForValue()
                .setIfAbsent(
                        key,
                        IdempotencyStatus.PROCESSANDO.name(),
                        PROCESSING_TTL
                );

        if (Boolean.TRUE.equals(acquired)) {
            return IdempotencyStatus.ADQUIRIDO;
        }

        String status = redisTemplate
                .opsForValue()
                .get(key);

        if (IdempotencyStatus.PROCESSADO.name().equals(status)) {
            return IdempotencyStatus.PROCESSADO;
        }

        if (IdempotencyStatus.PROCESSANDO.name().equals(status)) {
            return IdempotencyStatus.PROCESSANDO;
        }

        return tryAcquireAgain(key);
    }

    private IdempotencyStatus tryAcquireAgain(String key) {

        Boolean acquired = redisTemplate
                .opsForValue()
                .setIfAbsent(
                        key,
                        IdempotencyStatus.PROCESSANDO.name(),
                        PROCESSING_TTL
                );

        if (Boolean.TRUE.equals(acquired)) {
            return IdempotencyStatus.ADQUIRIDO;
        }

        return IdempotencyStatus.PROCESSANDO;
    }

    public void markProcessed(String eventId) {

        redisTemplate
                .opsForValue()
                .set(
                        buildKey(eventId),
                        IdempotencyStatus.PROCESSADO.name(),
                        PROCESSED_TTL
                );
    }

    public void release(String eventId) {
        redisTemplate.delete(buildKey(eventId));
    }

    private String buildKey(String eventId) {
        return "fraud:idempotency:event:" + eventId;
    }
}