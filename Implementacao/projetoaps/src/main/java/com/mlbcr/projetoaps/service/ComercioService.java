package com.mlbcr.projetoaps.service;

import org.springframework.stereotype.Service;
import com.mlbcr.projetoaps.model.Estoque;
import com.mlbcr.projetoaps.model.Produto;
import com.mlbcr.projetoaps.model.Loja;
import com.mlbcr.projetoaps.repository.EstoqueRepository;
import com.mlbcr.projetoaps.strategy.PrioridadeStrategy;
import com.mlbcr.projetoaps.strategy.DescontoStrategy;

import java.util.Optional;

@Service
public class ComercioService {

    private final EstoqueRepository estoqueRepository;
    private final PrioridadeStrategy prioridadeStrategy;
    private final DescontoStrategy descontoStrategy;

    public ComercioService(
            EstoqueRepository estoqueRepository,
            PrioridadeStrategy prioridadeStrategy,
            DescontoStrategy descontoStrategy) {
        this.estoqueRepository = estoqueRepository;
        this.prioridadeStrategy = prioridadeStrategy;
        this.descontoStrategy = descontoStrategy;
    }

    // Verifica se a loja do Méier (ou outra) precisa de reposição de emergência
    public boolean verificarNecessidadeReposicao(Produto produto, Loja loja) {
        Optional<Estoque> estoqueOpt = estoqueRepository.findByProdutoAndLoja(produto, loja);
        
        if (estoqueOpt.isEmpty()) {
            return true; // Se não tem registro de estoque, precisa repor
        }

        Estoque estoque = estoqueOpt.get();
        int limiteMinimo = prioridadeStrategy.obterQuantidadeMinima(estoque);

        // Retorna true se o estoque atual estiver abaixo do limite configurado pela política da loja
        return estoque.getQuantidade() < limiteMinimo;
    }

    // Consulta o preço de tabela do produto adaptado à região da loja
    public Double consultarPrecoParaLoja(Produto produto, Loja loja) {
        return descontoStrategy.calcularPrecoComDesconto(produto, loja);
    }
}