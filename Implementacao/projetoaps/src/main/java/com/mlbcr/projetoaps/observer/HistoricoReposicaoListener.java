package com.mlbcr.projetoaps.observer;

import org.springframework.stereotype.Component;
import com.mlbcr.projetoaps.service.RelatorioService;

@Component
public class HistoricoReposicaoListener implements ReposicaoAcaoListener {

    private final RelatorioService relatorioService;

    public HistoricoReposicaoListener(RelatorioService relatorioService) {
        this.relatorioService = relatorioService;
    }

    @Override
    public void onAcaoReposicao(ReposicaoEvent event) {
        relatorioService.registrarHistoricoCompra(
            event.getOrdemCompra(), 
            event.getMensagem()
        );
    }
}