package br.com.frauddetection.engine.rules;

import br.com.frauddetection.events.TransactionEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.ZoneId;

@Component
public class UnusualHourTransactionRule implements FraudRule {

    private final int horaInicio;
    private final int horaFim;
    private final ZoneId zona;

    public UnusualHourTransactionRule(
            @Value("${fraud.rules.unusual-hour.start}")
            int horaInicio,

            @Value("${fraud.rules.unusual-hour.end}")
            int horaFim,

            @Value("${fraud.rules.unusual-hour.zone}")
            String zona
    ) {
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
        this.zona = ZoneId.of(zona);
    }

    @Override
    public FraudRuleResult evaluate(TransactionEvent event) {

        LocalTime horarioTransacao =
                event.getDataHoraTransacao()
                        .atZone(zona)
                        .toLocalTime();

        int hora = horarioTransacao.getHour();

        boolean suspeita =
                hora >= horaInicio
                        && hora < horaFim;

        if (suspeita) {
            return new FraudRuleResult(
                    "TRANSACAO_HORARIO_INCOMUM",
                    true,
                    "Transação realizada em horário incomum"
            );
        }

        return new FraudRuleResult(
                "TRANSACAO_HORARIO_INCOMUM",
                false,
                null
        );
    }
}