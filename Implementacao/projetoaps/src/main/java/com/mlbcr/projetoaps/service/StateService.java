package com.mlbcr.projetoaps.service;

import org.springframework.stereotype.Service;

import com.mlbcr.projetoaps.model.Estoque;
import com.mlbcr.projetoaps.model.Produto;
import com.mlbcr.projetoaps.state.AlertaState;
import com.mlbcr.projetoaps.state.CriticoState;
import com.mlbcr.projetoaps.state.EsgotadoState;
import com.mlbcr.projetoaps.state.EstadoEstoque;
import com.mlbcr.projetoaps.state.NormalState;

@Service
public class StateService {
    private static final int LIMITE_CRITICO = 20;
    private static final int LIMITE_ALERTA = 50;

    public void atualizarEstado(Produto produto, Estoque estoque) {

        Integer quantidade = estoque.getQuantidade();

        int qtd = quantidade == null ? 0 : quantidade.intValue();

        EstadoEstoque estado;

        if (qtd == 0) {
            estado = new EsgotadoState();
        } else if (qtd <= LIMITE_CRITICO) {
            estado = new CriticoState();
        } else if (qtd <= LIMITE_ALERTA) {
            estado = new AlertaState();
        } else {
            estado = new NormalState();
        }

        estoque.setEstado(estado.getNome());
    }
}