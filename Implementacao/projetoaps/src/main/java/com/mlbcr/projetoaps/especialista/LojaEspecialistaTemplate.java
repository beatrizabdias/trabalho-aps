package com.mlbcr.projetoaps.especialista;

import com.mlbcr.projetoaps.dto.VendaRespostaDTO;
import com.mlbcr.projetoaps.service.VendaOperacaoService;

public abstract class LojaEspecialistaTemplate implements LojaEspecialista {

    private final VendaOperacaoService vendaOperacaoService;

    public LojaEspecialistaTemplate(VendaOperacaoService vendaOperacaoService) {
        this.vendaOperacaoService = vendaOperacaoService;
    }

    @Override
    public boolean atendeLoja(Long lojaId) {
        return getLojaId().equals(lojaId);
    }

    @Override
    public final VendaRespostaDTO registrarVenda(
            Long produtoId,
            Long lojaId,
            Integer quantidade) {

        validarLoja(lojaId);
        aplicarRegraEspecifica(produtoId, lojaId, quantidade);

        return vendaOperacaoService.registrarVenda(produtoId, lojaId, quantidade);
    }

    private void validarLoja(Long lojaId) {
        if (!atendeLoja(lojaId)) {
            throw new RuntimeException("Especialista incompatível com a loja informada");
        }
    }

    protected void aplicarRegraEspecifica(
            Long produtoId,
            Long lojaId,
            Integer quantidade) {
    }
}