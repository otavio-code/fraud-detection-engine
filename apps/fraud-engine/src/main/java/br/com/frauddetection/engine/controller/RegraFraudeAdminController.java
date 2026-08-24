package br.com.frauddetection.engine.controller;

import br.com.frauddetection.engine.configuration.ConfiguracaoRegraService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/regras")
public class RegraFraudeAdminController {

    private final ConfiguracaoRegraService configuracaoRegraService;

    public RegraFraudeAdminController(
            ConfiguracaoRegraService configuracaoRegraService
    ) {
        this.configuracaoRegraService =
                configuracaoRegraService;
    }

    @PutMapping("/transacao-valor-alto/limite")
    public void atualizarLimite(
            @Valid
            @RequestBody
            UpdateLimitRequest request
    ) {

        configuracaoRegraService
                .atualizarLimiteTransacaoValorAlto(
                        request.limite()
                );
    }
}