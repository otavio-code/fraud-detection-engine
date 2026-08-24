package br.com.frauddetection.engine.controller;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record UpdateLimitRequest(

        @NotNull(
                message = "O limite é obrigatório"
        )
        @Positive(
                message = "O limite deve ser maior que zero"
        )
        BigDecimal limite

) {
}