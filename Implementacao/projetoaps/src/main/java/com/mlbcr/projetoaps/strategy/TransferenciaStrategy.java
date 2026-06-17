package com.mlbcr.projetoaps.strategy;

import org.springframework.stereotype.Component;

import com.mlbcr.projetoaps.model.Estoque;

@Component
public class TransferenciaStrategy implements ReposicaoStrategy {

    private static final int QUANTIDADE_TRANSFERENCIA = 15;
    private final PrioridadeStrategy prioridadeStrategy;

    public TransferenciaStrategy(PrioridadeStrategy prioridadeStrategy) {
        this.prioridadeStrategy = prioridadeStrategy;
    }

    @Override
    public boolean podeAplicar(Estoque estoque) {
        if (estoque == null) {
            return false;
        }

        int quantidadeAtual = estoque.getQuantidade() == null ? 0 : estoque.getQuantidade();
        return quantidadeAtual > prioridadeStrategy.obterLimiteSeguroOrigem();
    }
}