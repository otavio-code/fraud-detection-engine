package br.com.frauddetection.engine.rules;

import br.com.frauddetection.events.TransactionEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class TransactionVelocityRule implements FraudRule {

    private static final String PREFIXO_CHAVE =
            "fraude:regras:velocidade:conta:";

    private final StringRedisTemplate redisTemplate;
    private final long limiteTransacoes;
    private final Duration janela;

    public TransactionVelocityRule(
            StringRedisTemplate redisTemplate,
            @Value("${fraud.rules.velocity.max-transactions}")
            long limiteTransacoes,
            @Value("${fraud.rules.velocity.window}")
            Duration janela
    ) {
        this.redisTemplate = redisTemplate;
        this.limiteTransacoes = limiteTransacoes;
        this.janela = janela;
    }

    @Override
    public FraudRuleResult evaluate(TransactionEvent event) {

        String chave = PREFIXO_CHAVE + event.getIdContaOrigem();

        Long quantidade = redisTemplate
                .opsForValue()
                .increment(chave);

        if (quantidade != null && quantidade == 1) {
            redisTemplate.expire(
                    chave,
                    janela
            );
        }

        boolean suspeita =
                quantidade != null
                        && quantidade > limiteTransacoes;

        if (suspeita) {
            return new FraudRuleResult(
                    "MUITAS_TRANSACOES_CURTO_PERIODO",
                    true,
                    "Quantidade de transações acima do limite no período"
            );
        }

        return new FraudRuleResult(
                "MUITAS_TRANSACOES_CURTO_PERIODO",
                false,
                null
        );
    }
}