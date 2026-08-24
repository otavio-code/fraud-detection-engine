package br.com.frauddetection.engine.controller;

import br.com.frauddetection.engine.configuration.ConfiguracaoRegraService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RegraFraudeAdminControllerTest {

    private ConfiguracaoRegraService configuracaoRegraService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {

        configuracaoRegraService =
                Mockito.mock(ConfiguracaoRegraService.class);

        RegraFraudeAdminController controller =
                new RegraFraudeAdminController(
                        configuracaoRegraService
                );

        mockMvc =
                MockMvcBuilders
                        .standaloneSetup(controller)
                        .build();
    }

    @Test
    void deveAtualizarLimiteDaRegraDeTransacaoValorAlto()
            throws Exception {

        // Act
        mockMvc.perform(
                        put(
                                "/admin/regras/transacao-valor-alto/limite"
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "limite": 20000.00
                                        }
                                        """
                                )
                )
                .andExpect(
                        status().isOk()
                );

        // Assert
        Mockito.verify(configuracaoRegraService)
                .atualizarLimiteTransacaoValorAlto(
                        new BigDecimal("20000.00")
                );
    }

    @Test
    void deveRejeitarLimiteNegativo()
            throws Exception {

        // Act
        mockMvc.perform(
                        put(
                                "/admin/regras/transacao-valor-alto/limite"
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "limite": -100.00
                                        }
                                        """
                                )
                )
                .andExpect(
                        status().isBadRequest()
                );

        // Assert
        Mockito.verifyNoInteractions(
                configuracaoRegraService
        );
    }

    @Test
    void deveRejeitarLimiteIgualAZero()
            throws Exception {

        // Act
        mockMvc.perform(
                        put(
                                "/admin/regras/transacao-valor-alto/limite"
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "limite": 0
                                        }
                                        """
                                )
                )
                .andExpect(
                        status().isBadRequest()
                );

        // Assert
        Mockito.verifyNoInteractions(
                configuracaoRegraService
        );
    }

    @Test
    void deveRejeitarLimiteNulo()
            throws Exception {

        // Act
        mockMvc.perform(
                        put(
                                "/admin/regras/transacao-valor-alto/limite"
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "limite": null
                                        }
                                        """
                                )
                )
                .andExpect(
                        status().isBadRequest()
                );

        // Assert
        Mockito.verifyNoInteractions(
                configuracaoRegraService
        );
    }
}