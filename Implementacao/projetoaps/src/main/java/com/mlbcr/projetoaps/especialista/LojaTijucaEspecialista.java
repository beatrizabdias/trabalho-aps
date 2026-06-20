package com.mlbcr.projetoaps.especialista;

import org.springframework.stereotype.Component;

import com.mlbcr.projetoaps.service.VendaOperacaoService;

@Component
public class LojaTijucaEspecialista extends LojaEspecialistaTemplate {

    public LojaTijucaEspecialista(VendaOperacaoService vendaOperacaoService) {
        super(vendaOperacaoService);
    }

    @Override
    public Long getLojaId() {
        return 2L;
    }

    @Override
    public String getNomeLoja() {
        return "Tijuca";
    }

    @Override
    protected void aplicarRegraEspecifica(
            Long produtoId,
            Long lojaId,
            Integer quantidade) {

        System.out.println("Especialista da Loja Tijuca processando venda.");
    }
}