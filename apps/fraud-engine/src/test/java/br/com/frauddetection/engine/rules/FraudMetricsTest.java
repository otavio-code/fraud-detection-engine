package br.com.frauddetection.engine.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.when;

class FraudMetricsTest {

    @Test
    void deveRegistrarTransacaoProcessada() {

        // Arrange
        MeterRegistry meterRegistry =
                Mockito.mock(MeterRegistry.class);

        Counter counter =
                Mockito.mock(Counter.class);

        when(
                meterRegistry.counter(
                        "fraud.transactions.processed.total"
                )
        ).thenReturn(counter);

        FraudMetrics fraudMetrics =
                new FraudMetrics(meterRegistry);

        // Act
        fraudMetrics.registrarTransacaoProcessada();

        // Assert
        Mockito.verify(counter)
                .increment();
    }

    @Test
    void deveRegistrarTransacaoSuspeita() {

        // Arrange
        MeterRegistry meterRegistry =
                Mockito.mock(MeterRegistry.class);

        Counter counter =
                Mockito.mock(Counter.class);

        when(
                meterRegistry.counter(
                        "fraud.transactions.suspicious.total"
                )
        ).thenReturn(counter);

        FraudMetrics fraudMetrics =
                new FraudMetrics(meterRegistry);

        // Act
        fraudMetrics.registrarTransacaoSuspeita();

        // Assert
        Mockito.verify(counter)
                .increment();
    }

    @Test
    void deveRegistrarRegraSuspeita() {

        // Arrange
        MeterRegistry meterRegistry =
                Mockito.mock(MeterRegistry.class);

        Counter counter =
                Mockito.mock(Counter.class);

        when(
                meterRegistry.counter(
                        "fraud.rules.triggered.total",
                        "regra",
                        "TRANSACAO_VALOR_ALTO"
                )
        ).thenReturn(counter);

        FraudMetrics fraudMetrics =
                new FraudMetrics(meterRegistry);

        // Act
        fraudMetrics.registrarRegraSuspeita(
                "TRANSACAO_VALOR_ALTO"
        );

        // Assert
        Mockito.verify(counter)
                .increment();
    }

    @Test
    void deveRegistrarEnvioParaDlt() {

        // Arrange
        MeterRegistry meterRegistry =
                Mockito.mock(MeterRegistry.class);

        Counter counter =
                Mockito.mock(Counter.class);

        when(
                meterRegistry.counter(
                        "fraud.transactions.dlt.total"
                )
        ).thenReturn(counter);

        FraudMetrics fraudMetrics =
                new FraudMetrics(meterRegistry);

        // Act
        fraudMetrics.registrarEnvioDlt();

        // Assert
        Mockito.verify(counter)
                .increment();
    }

    @Test
    void deveFinalizarTempoDeProcessamento() {

        // Arrange
        MeterRegistry meterRegistry =
                Mockito.mock(MeterRegistry.class);

        Timer timer =
                Mockito.mock(Timer.class);

        Timer.Sample sample =
                Mockito.mock(Timer.Sample.class);

        when(
                meterRegistry.timer(
                        "fraud.transaction.processing.duration"
                )
        ).thenReturn(timer);

        FraudMetrics fraudMetrics =
                new FraudMetrics(meterRegistry);

        // Act
        fraudMetrics.finalizarProcessamento(sample);

        // Assert
        Mockito.verify(sample)
                .stop(timer);
    }
}