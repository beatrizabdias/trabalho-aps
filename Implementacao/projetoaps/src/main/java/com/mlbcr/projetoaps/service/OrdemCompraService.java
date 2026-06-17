package com.mlbcr.projetoaps.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mlbcr.projetoaps.model.Estoque;
import com.mlbcr.projetoaps.model.Loja;
import com.mlbcr.projetoaps.model.OrdemCompra;
import com.mlbcr.projetoaps.model.Produto;
import com.mlbcr.projetoaps.repository.EstoqueRepository;
import com.mlbcr.projetoaps.repository.LojaRepository;
import com.mlbcr.projetoaps.repository.OrdemCompraRepository;
import com.mlbcr.projetoaps.repository.ProdutoRepository;

@Service
public class OrdemCompraService {

    private final OrdemCompraRepository ordemCompraRepository;
    private final EstoqueRepository estoqueRepository;
    private final StateService stateService;
    private final ProdutoRepository produtoRepository;
    private final LojaRepository lojaRepository;

    public OrdemCompraService(
        OrdemCompraRepository ordemCompraRepository,
        EstoqueRepository estoqueRepository,
        StateService stateService,
        ProdutoRepository produtoRepository,
        LojaRepository lojaRepository) {

        this.ordemCompraRepository = ordemCompraRepository;
        this.estoqueRepository = estoqueRepository;
        this.stateService = stateService;
        this.produtoRepository = produtoRepository;
        this.lojaRepository = lojaRepository;
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

        stateService.atualizarEstado(
                ordemCompra.getProduto(),
                estoque
        );

        estoqueRepository.save(estoque);

        ordemCompra.setStatus("CONCLUIDA");

        return ordemCompraRepository.save(ordemCompra);
    }
    @Transactional
    public OrdemCompra criarCompraManual(
        Long produtoId,
        Long lojaId,
        Integer quantidade) {

        Produto produto = produtoRepository
        .findById(produtoId)
        .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        Loja loja = lojaRepository
        .findById(lojaId)
        .orElseThrow(() -> new RuntimeException("Loja não encontrada"));

        OrdemCompra ordem = new OrdemCompra();

        ordem.setProduto(produto);
        ordem.setFornecedor(produto.getFornecedor());
        ordem.setLoja(loja);
        ordem.setQuantidade(quantidade);
        ordem.setDataCriacao(LocalDateTime.now());
        ordem.setStatus("PENDENTE");

        OrdemCompra ordemSalva = ordemCompraRepository.save(ordem);

        new Thread(() -> {
        try {

                Thread.sleep(5000);

                receberCompra(ordemSalva.getId());

        } catch (Exception e) {
                e.printStackTrace();
        }
        }).start();

        return ordemSalva;
     }
}