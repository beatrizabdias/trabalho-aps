package com.mlbcr.projetoaps.observer;

import com.mlbcr.projetoaps.model.Loja;
import com.mlbcr.projetoaps.model.Produto;

public interface ReposicaoObserver {
    void atualizar(Produto produto, Loja loja);
}