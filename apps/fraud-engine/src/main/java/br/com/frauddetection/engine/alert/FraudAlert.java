package br.com.frauddetection.engine.alert;

import java.time.Instant;
import java.util.List;

public record FraudAlert(
        String idAlerta,
        String idEvento,
        String idTransacao,
        String idCliente,
        List<String> regras,
        Instant dataHora
) {
}