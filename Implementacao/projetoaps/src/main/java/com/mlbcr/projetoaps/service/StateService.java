package com.mlbcr.projetoaps.service;

import org.springframework.stereotype.Service;

import com.mlbcr.projetoaps.model.Estoque;
import com.mlbcr.projetoaps.model.Produto;
import com.mlbcr.projetoaps.state.*;

@Service
public class StateService {

    public void atualizarEstado(Produto produto, Estoque estoque) {

        EstadoEstoque estado;

        int qtd = estoque.getQuantidade();

        if (qtd == 0) {
            estado = new EsgotadoState();

        } else if (qtd < 20) {
            estado = new CriticoState();

        } else if (qtd <= 60) {
            estado = new AlertaState();

        } else {
            estado = new NormalState();
        }

        estoque.setEstado(estado.getNome());
    }
}