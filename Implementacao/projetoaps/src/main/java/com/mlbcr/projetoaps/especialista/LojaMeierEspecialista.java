package com.mlbcr.projetoaps.especialista;

import org.springframework.stereotype.Component;

import com.mlbcr.projetoaps.service.VendaOperacaoService;

@Component
public class LojaMeierEspecialista extends LojaEspecialistaTemplate {

    public LojaMeierEspecialista(VendaOperacaoService vendaOperacaoService) {
        super(vendaOperacaoService);
    }

    @Override
    public Long getLojaId() {
        return 1L;
    }

    @Override
    public String getNomeLoja() {
        return "Méier";
    }
}