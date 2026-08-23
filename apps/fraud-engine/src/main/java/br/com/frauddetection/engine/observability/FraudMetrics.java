package br.com.frauddetection.engine.observability;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class FraudMetrics {

    private final MeterRegistry meterRegistry;

    public FraudMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void registrarTransacaoProcessada() {
        meterRegistry
                .counter("fraud.transactions.processed.total")
                .increment();
    }

    public void registrarTransacaoSuspeita() {
        meterRegistry
                .counter("fraud.transactions.suspicious.total")
                .increment();
    }

    public void registrarRegraSuspeita(String regra) {
        meterRegistry
                .counter(
                        "fraud.rules.triggered.total",
                        "regra",
                        regra
                )
                .increment();
    }

    public void registrarEnvioDlt() {
        meterRegistry
                .counter("fraud.transactions.dlt.total")
                .increment();
    }

    public Timer.Sample iniciarProcessamento() {
        return Timer.start(meterRegistry);
    }

    public void finalizarProcessamento(Timer.Sample sample) {
        sample.stop(
                meterRegistry.timer(
                        "fraud.transaction.processing.duration"
                )
        );
    }
}