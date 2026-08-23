package br.com.frauddetection.engine.rules;

import br.com.frauddetection.engine.configuration.ConfiguracaoRegraService;
import br.com.frauddetection.events.TransactionEvent;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class HighValueTransactionRule implements FraudRule {

    private final ConfiguracaoRegraService configuracaoRegraService;

    public HighValueTransactionRule(
            ConfiguracaoRegraService configuracaoRegraService
    ) {
        this.configuracaoRegraService = configuracaoRegraService;
    }

    @Override
    public FraudRuleResult evaluate(TransactionEvent event) {

        BigDecimal limite =
                configuracaoRegraService
                        .obterLimiteTransacaoValorAlto();

        boolean suspeita =
                event.getValorTransacao().compareTo(limite) > 0;

        if (suspeita) {
            return new FraudRuleResult(
                    "TRANSACAO_VALOR_ALTO",
                    true,
                    "Transação acima do limite de R$ " + limite
            );
        }

        return new FraudRuleResult(
                "TRANSACAO_VALOR_ALTO",
                false,
                null
        );
    }
}