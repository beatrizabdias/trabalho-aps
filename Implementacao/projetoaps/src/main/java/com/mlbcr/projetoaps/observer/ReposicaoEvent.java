package com.mlbcr.projetoaps.observer;

import com.mlbcr.projetoaps.model.OrdemCompra;

import lombok.Getter;

@Getter
public class ReposicaoEvent {
    private final OrdemCompra ordemCompra;
    private final String mensagem;

    public ReposicaoEvent(OrdemCompra ordemCompra, String mensagem) {
        this.ordemCompra = ordemCompra;
        this.mensagem = mensagem;
    }
}