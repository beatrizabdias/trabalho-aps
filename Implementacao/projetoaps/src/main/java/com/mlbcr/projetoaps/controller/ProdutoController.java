package com.mlbcr.projetoaps.controller;

import java.util.List;
import org.springframework.web.bind.annotation.*;
import com.mlbcr.projetoaps.model.Produto;
import com.mlbcr.projetoaps.model.Loja;
import com.mlbcr.projetoaps.repository.ProdutoRepository;
import com.mlbcr.projetoaps.repository.LojaRepository;

@RestController
@RequestMapping("/produtos")
public class ProdutoController {
    
    private final ProdutoRepository produtoRepository;
    private final LojaRepository lojaRepository; // Injetado para poder validar a loja

    public ProdutoController(ProdutoRepository produtoRepository, LojaRepository lojaRepository) {
        this.produtoRepository = produtoRepository;
        this.lojaRepository = lojaRepository;
    }

    @GetMapping
    public List<Produto> listarProdutos() {
        return produtoRepository.findAll();
    }

    // NOVO ENDPOINT: Calcula o preço sob demanda para uma determinada loja
    @GetMapping("/{id}/preco-loja/{lojaId}")
    public Double obterPrecoSobMedida(@PathVariable Long id, @PathVariable Long lojaId) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
        
        Loja loja = lojaRepository.findById(lojaId)
                .orElseThrow(() -> new RuntimeException("Loja não encontrada"));

        Double precoOriginal = produto.getValorVenda();
        if (precoOriginal == null) return 0.0;

        // Regra aplicada diretamente aqui sem serviços adicionais
        if ("Tijuca".equalsIgnoreCase(loja.getNome())) {
            return precoOriginal * 0.85; // 15% de desconto na Tijuca
        }

        return precoOriginal;
    }
}