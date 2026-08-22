package br.com.frauddetection.engine.rules;

import br.com.frauddetection.events.TransactionEvent;

public interface FraudRule {

    FraudRuleResult evaluate(TransactionEvent event);
}