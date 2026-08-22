package br.com.frauddetection.engine.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ConfiguracaoRegraService {

    private static final String CHAVE_LIMITE_TRANSACAO_VALOR_ALTO =
            "fraude:regras:transacao-valor-alto:limite";

    private final StringRedisTemplate redisTemplate;
    private final BigDecimal limitePadraoTransacaoValorAlto;

    public ConfiguracaoRegraService(
            StringRedisTemplate redisTemplate,
            @Value("${fraud.rules.high-value.limit}")
            BigDecimal limitePadraoTransacaoValorAlto
    ) {
        this.redisTemplate = redisTemplate;
        this.limitePadraoTransacaoValorAlto =
                limitePadraoTransacaoValorAlto;
    }

    public BigDecimal obterLimiteTransacaoValorAlto() {

        String limite = redisTemplate
                .opsForValue()
                .get(CHAVE_LIMITE_TRANSACAO_VALOR_ALTO);

        if (limite == null) {
            return limitePadraoTransacaoValorAlto;
        }

        return new BigDecimal(limite);
    }

    public void atualizarLimiteTransacaoValorAlto(BigDecimal limite) {

        redisTemplate
                .opsForValue()
                .set(
                        CHAVE_LIMITE_TRANSACAO_VALOR_ALTO,
                        limite.toPlainString()
                );
    }
}