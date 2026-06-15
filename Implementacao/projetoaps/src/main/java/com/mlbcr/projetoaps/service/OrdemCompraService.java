package com.mlbcr.projetoaps.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mlbcr.projetoaps.model.Estoque;
import com.mlbcr.projetoaps.model.OrdemCompra;
import com.mlbcr.projetoaps.repository.EstoqueRepository;
import com.mlbcr.projetoaps.repository.OrdemCompraRepository;

@Service
public class OrdemCompraService {

    private final OrdemCompraRepository ordemCompraRepository;
    private final EstoqueRepository estoqueRepository;

    public OrdemCompraService(
            OrdemCompraRepository ordemCompraRepository,
            EstoqueRepository estoqueRepository) {

        this.ordemCompraRepository = ordemCompraRepository;
        this.estoqueRepository = estoqueRepository;
    }

    @Transactional
    public OrdemCompra receberCompra(Long ordemId) {

        OrdemCompra ordemCompra = ordemCompraRepository.findById(ordemId)
                .orElseThrow(() -> new RuntimeException("Ordem de compra não encontrada"));

        if (!"PENDENTE".equalsIgnoreCase(ordemCompra.getStatus())) {
            throw new RuntimeException("Esta ordem de compra já foi processada");
        }

        Estoque estoque = estoqueRepository
                .findByProdutoAndLoja(
                        ordemCompra.getProduto(),
                        ordemCompra.getLoja()
                )
                .orElseThrow(() -> new RuntimeException("Estoque não encontrado"));

        estoque.setQuantidade(
                estoque.getQuantidade() + ordemCompra.getQuantidade()
        );

        estoqueRepository.save(estoque);

        ordemCompra.setStatus("CONCLUIDA");

        return ordemCompraRepository.save(ordemCompra);
    }
}