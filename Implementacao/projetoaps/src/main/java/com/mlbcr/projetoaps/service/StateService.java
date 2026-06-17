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
    private int qtdAlerta = 30;

    public void atualizarEstado(Produto produto, Estoque estoque) {

        Integer quantidade = estoque.getQuantidade();
        Integer minimo = produto.getQtdMinima();

        int qtd = quantidade == null ? 0 : quantidade.intValue();
        int qtdMinima = minimo == null ? 0 : minimo.intValue();

        System.out.println(
            "Qtd=" + qtd +
            " Min=" + qtdMinima
        );

        EstadoEstoque estado;

        if (qtd == 0) {
            estado = new EsgotadoState();
        } else if (qtd <= qtdMinima) {
            estado = new CriticoState();
        } else if (qtd <= qtdAlerta) {
            estado = new AlertaState();
        } else {
            estado = new NormalState();
        }

        System.out.println("Novo estado = " + estado.getNome());

        estoque.setEstado(estado.getNome());
    }
}