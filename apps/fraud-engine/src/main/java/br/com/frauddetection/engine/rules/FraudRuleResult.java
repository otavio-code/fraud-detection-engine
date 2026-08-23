package br.com.frauddetection.engine.rules;

public record FraudRuleResult(
        String regra,
        boolean suspeita,
        String motivo
) {
}