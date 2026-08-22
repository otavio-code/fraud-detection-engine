package br.com.frauddetection.engine.rules;

import br.com.frauddetection.events.TransactionEvent;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class HighValueTransactionRule implements FraudRule {

    private static final BigDecimal LIMIT =
            new BigDecimal("10000.00");

    @Override
    public FraudRuleResult evaluate(TransactionEvent event) {

        boolean suspeita =
                event.getValorTransacao().compareTo(LIMIT) > 0;

        if (suspeita) {
            return new FraudRuleResult(
                    "TRANSACAO_VALOR_ALTO",
                    true,
                    "Transação acima do limite de R$ 10.000,00"
            );
        }

        return new FraudRuleResult(
                "TRANSACAO_VALOR_ALTO",
                false,
                null
        );
    }
}