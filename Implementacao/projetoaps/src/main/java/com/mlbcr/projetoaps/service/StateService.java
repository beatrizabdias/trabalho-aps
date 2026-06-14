package com.mlbcr.projetoaps.service;

import org.springframework.stereotype.Service;

import com.mlbcr.projetoaps.model.Estoque;
import com.mlbcr.projetoaps.model.Produto;
import com.mlbcr.projetoaps.state.*;

@Service
public class StateService {

    public void atualizarEstado(Produto produto, Estoque estoque) {

        EstadoEstoque estado;

        if (estoque.getQuantidade() == 0) {
            estado = new EsgotadoState();

        } else if (estoque.getQuantidade() <= produto.getQtdMinima()) {
            estado = new AlertaState();

        } else {
            estado = new NormalState();
        }

        // só funciona se você tiver esse campo no Estoque
        estoque.setEstado(estado.getNome());
    }
}