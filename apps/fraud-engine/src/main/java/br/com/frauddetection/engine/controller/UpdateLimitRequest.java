package br.com.frauddetection.engine.controller;

import java.math.BigDecimal;

public record UpdateLimitRequest(
        BigDecimal limite
) {
}