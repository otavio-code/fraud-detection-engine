package br.com.frauddetection.engine.rules;

import br.com.frauddetection.events.TransactionEvent;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FraudRuleEngine {

    private final List<FraudRule> rules;

    public FraudRuleEngine(List<FraudRule> rules) {
        this.rules = rules;
    }

    public List<FraudRuleResult> evaluate(TransactionEvent event) {

        return rules.stream()
                .map(rule -> rule.evaluate(event))
                .filter(FraudRuleResult::suspeita)
                .toList();
    }
}