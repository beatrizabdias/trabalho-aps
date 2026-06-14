package com.mlbcr.projetoaps.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.mlbcr.projetoaps.model.Estoque;
import com.mlbcr.projetoaps.model.Loja;
import com.mlbcr.projetoaps.model.Produto;
import com.mlbcr.projetoaps.model.Venda;
import com.mlbcr.projetoaps.repository.EstoqueRepository;
import com.mlbcr.projetoaps.repository.LojaRepository;
import com.mlbcr.projetoaps.repository.ProdutoRepository;
import com.mlbcr.projetoaps.repository.VendaRepository;

import com.mlbcr.projetoaps.observer.ReposicaoObserver;
import com.mlbcr.projetoaps.service.StateService;

@Service
public class VendaService {
    private final VendaRepository vendaRepository;
    private final EstoqueRepository estoqueRepository;
    private final ProdutoRepository produtoRepository;
    private final LojaRepository lojaRepository;
    //private final ReposicaoService reposicaoService;
    private final ReposicaoObserver reposicaoObserver;
    private final StateService stateService;

    public VendaService(VendaRepository vendaRepository,EstoqueRepository estoqueRepository,
        ProdutoRepository produtoRepository,LojaRepository lojaRepository, ReposicaoObserver reposicaoObserver, StateService stateService) {
        this.vendaRepository = vendaRepository;
        this.estoqueRepository = estoqueRepository;
        this.produtoRepository = produtoRepository;
        this.lojaRepository = lojaRepository;
        this.reposicaoObserver = reposicaoObserver;
        this.stateService = stateService;
    }

    public Venda registrarVenda(
        Long produtoId,
        Long lojaId,
        Integer quantidade) {
        
        Produto produto = produtoRepository
            .findById(produtoId)
            .orElseThrow(
                () -> new RuntimeException("Produto não encontrado")
            );

        Loja loja = lojaRepository
            .findById(lojaId)
            .orElseThrow(
                () -> new RuntimeException("Loja não encontrada")
            );
        
        Estoque estoque = estoqueRepository
            .findByProdutoAndLoja(produto, loja)
            .orElseThrow(
                () -> new RuntimeException("Estoque não encontrado")
            );
        
        if (estoque.getQuantidade() < quantidade) {
            throw new RuntimeException("Estoque insuficiente");
        }
        estoque.setQuantidade(
            estoque.getQuantidade() - quantidade
        );
        estoqueRepository.save(estoque);

        stateService.atualizarEstado(
            produto,
            estoque
        );

        Venda venda = new Venda();

        venda.setProduto(produto);
        venda.setLoja(loja);
        venda.setQuantidade(quantidade);
        venda.setDataVenda(LocalDateTime.now());

        Venda vendaSalva = vendaRepository.save(venda);
        
        // verificamos se precisa de reposição
        reposicaoObserver.atualizar(produto, loja);
        return vendaSalva;
    }
}
