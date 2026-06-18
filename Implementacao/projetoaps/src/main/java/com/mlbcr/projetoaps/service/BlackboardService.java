package com.mlbcr.projetoaps.service;

import org.springframework.stereotype.Service;

import com.mlbcr.projetoaps.model.Estoque;
import com.mlbcr.projetoaps.model.Loja;
import com.mlbcr.projetoaps.model.OrdemCompra;
import com.mlbcr.projetoaps.model.Produto;
import com.mlbcr.projetoaps.model.Transferencia;
import com.mlbcr.projetoaps.model.Venda;
import com.mlbcr.projetoaps.repository.EstoqueRepository;
import com.mlbcr.projetoaps.repository.OrdemCompraRepository;
import com.mlbcr.projetoaps.repository.TransferenciaRepository;
import com.mlbcr.projetoaps.repository.VendaRepository;

@Service
public class BlackboardService {

    private final EstoqueRepository estoqueRepository;
    private final VendaRepository vendaRepository;
    private final TransferenciaRepository transferenciaRepository;
    private final OrdemCompraRepository ordemCompraRepository;

    public BlackboardService(
            EstoqueRepository estoqueRepository,
            VendaRepository vendaRepository,
            TransferenciaRepository transferenciaRepository,
            OrdemCompraRepository ordemCompraRepository) {

        this.estoqueRepository = estoqueRepository;
        this.vendaRepository = vendaRepository;
        this.transferenciaRepository = transferenciaRepository;
        this.ordemCompraRepository = ordemCompraRepository;
    }

    public Estoque buscarEstoque(Produto produto, Loja loja) {
        return estoqueRepository.findByProdutoAndLoja(produto, loja)
                .orElseThrow(() -> new RuntimeException("Estoque não encontrado"));
    }

    public Estoque buscarEstoqueOuNulo(Produto produto, Loja loja) {
        return estoqueRepository.findByProdutoAndLoja(produto, loja)
                .orElse(null);
    }

    public Estoque salvarEstoque(Estoque estoque) {
        return estoqueRepository.save(estoque);
    }

    public Venda registrarVenda(Venda venda) {
        return vendaRepository.save(venda);
    }

    public Transferencia registrarTransferencia(Transferencia transferencia) {
        return transferenciaRepository.save(transferencia);
    }

    public OrdemCompra registrarOrdemCompra(OrdemCompra ordemCompra) {
        return ordemCompraRepository.save(ordemCompra);
    }
}