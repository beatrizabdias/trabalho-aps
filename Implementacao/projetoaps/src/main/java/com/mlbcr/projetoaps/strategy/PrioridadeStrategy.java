package com.mlbcr.projetoaps.strategy;

import com.mlbcr.projetoaps.model.Estoque;
import org.springframework.stereotype.Component;

@Component
public class PrioridadeStrategy {

    private static final int LIMITE_SEGURANCA_ORIGEM = 51;
    private static final int META_TRANSFERENCIA = 35;
    private static final int META_COMPRA = 100;

    public int obterLimiteSeguroOrigem() {
        return LIMITE_SEGURANCA_ORIGEM;
    }

    public int obterMetaTransferencia() {
        return META_TRANSFERENCIA;
    }

    public int obterMetaCompra() {
        return META_COMPRA;
    }

    public int calcularQuantidadeTransferencia(Estoque estoque) {
        int quantidadeAtual = estoque.getQuantidade() == null ? 0 : estoque.getQuantidade();
        return Math.max(0, META_TRANSFERENCIA - quantidadeAtual);
    }

    public int calcularQuantidadeCompra(Estoque estoque) {
        int quantidadeAtual = estoque.getQuantidade() == null ? 0 : estoque.getQuantidade();
        return Math.max(0, META_COMPRA - quantidadeAtual);
    }
}