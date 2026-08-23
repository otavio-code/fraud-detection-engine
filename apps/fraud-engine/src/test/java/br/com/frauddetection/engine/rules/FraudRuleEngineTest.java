package br.com.frauddetection.engine.rules;

import br.com.frauddetection.events.TransactionEvent;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class FraudRuleEngineTest {

    @Test
    void deveRetornarApenasResultadosSuspeitos() {

        // Arrange
        FraudRule regraSuspeita =
                Mockito.mock(FraudRule.class);

        FraudRule regraNaoSuspeita =
                Mockito.mock(FraudRule.class);

        TransactionEvent event =
                Mockito.mock(TransactionEvent.class);

        when(
                regraSuspeita.evaluate(event)
        ).thenReturn(
                new FraudRuleResult(
                        "REGRA_SUSPEITA",
                        true,
                        "Motivo de teste"
                )
        );

        when(
                regraNaoSuspeita.evaluate(event)
        ).thenReturn(
                new FraudRuleResult(
                        "REGRA_NORMAL",
                        false,
                        null
                )
        );

        FraudRuleEngine engine =
                new FraudRuleEngine(
                        List.of(
                                regraSuspeita,
                                regraNaoSuspeita
                        )
                );

        //Act
        List<FraudRuleResult> resultado =
                engine.evaluate(event);

        //Assert
        assertEquals(
                1,
                resultado.size()
        );

        assertEquals(
                "REGRA_SUSPEITA",
                resultado.getFirst().regra()
        );

        Mockito.verify(regraSuspeita)
                .evaluate(event);

        Mockito.verify(regraNaoSuspeita)
                .evaluate(event);
    }

    @Test
    void deveRetornarListaVaziaQuandoNenhumaRegraIdentificarSuspeita() {

        //Arrange
        FraudRule regraUm =
                Mockito.mock(FraudRule.class);

        FraudRule regraDois =
                Mockito.mock(FraudRule.class);

        TransactionEvent event =
                Mockito.mock(TransactionEvent.class);

        when(
                regraUm.evaluate(event)
        ).thenReturn(
                new FraudRuleResult(
                        "REGRA_UM",
                        false,
                        null
                )
        );

        when(
                regraDois.evaluate(event)
        ).thenReturn(
                new FraudRuleResult(
                        "REGRA_DOIS",
                        false,
                        null
                )
        );

        FraudRuleEngine engine =
                new FraudRuleEngine(
                        List.of(
                                regraUm,
                                regraDois
                        )
                );

        //Act
        List<FraudRuleResult> resultado =
                engine.evaluate(event);

        //Assert
        assertTrue(resultado.isEmpty());

        Mockito.verify(regraUm)
                .evaluate(event);

        Mockito.verify(regraDois)
                .evaluate(event);
    }

    @Test
    void deveRetornarTodosOsResultadosSuspeitos() {

        // Arrange
        FraudRule regraUm =
                Mockito.mock(FraudRule.class);

        FraudRule regraDois =
                Mockito.mock(FraudRule.class);

        TransactionEvent event =
                Mockito.mock(TransactionEvent.class);

        when(
                regraUm.evaluate(event)
        ).thenReturn(
                new FraudRuleResult(
                        "REGRA_UM",
                        true,
                        "Motivo da regra um"
                )
        );

        when(
                regraDois.evaluate(event)
        ).thenReturn(
                new FraudRuleResult(
                        "REGRA_DOIS",
                        true,
                        "Motivo da regra dois"
                )
        );

        FraudRuleEngine engine =
                new FraudRuleEngine(
                        List.of(
                                regraUm,
                                regraDois
                        )
                );

        // Act
        List<FraudRuleResult> resultado =
                engine.evaluate(event);

        // Assert
        assertEquals(
                2,
                resultado.size()
        );

        assertEquals(
                "REGRA_UM",
                resultado.get(0).regra()
        );

        assertEquals(
                "REGRA_DOIS",
                resultado.get(1).regra()
        );

        Mockito.verify(regraUm)
                .evaluate(event);

        Mockito.verify(regraDois)
                .evaluate(event);
    }
}