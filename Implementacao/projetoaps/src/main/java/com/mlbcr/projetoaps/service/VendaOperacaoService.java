package com.mlbcr.projetoaps.service;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.mlbcr.projetoaps.dto.ReposicaoRespostaDTO;
import com.mlbcr.projetoaps.dto.VendaRespostaDTO;
import com.mlbcr.projetoaps.model.Estoque;
import com.mlbcr.projetoaps.model.Loja;
import com.mlbcr.projetoaps.model.Produto;
import com.mlbcr.projetoaps.model.Venda;
import com.mlbcr.projetoaps.repository.LojaRepository;
import com.mlbcr.projetoaps.repository.ProdutoRepository;

@Service
public class VendaOperacaoService {

    private final ProdutoRepository produtoRepository;
    private final LojaRepository lojaRepository;
    private final ReposicaoService reposicaoService;
    private final StateService stateService;
    private final BlackboardService blackboardService;

    public VendaOperacaoService(
            ProdutoRepository produtoRepository,
            LojaRepository lojaRepository,
            ReposicaoService reposicaoService,
            StateService stateService,
            BlackboardService blackboardService) {

        this.produtoRepository = produtoRepository;
        this.lojaRepository = lojaRepository;
        this.reposicaoService = reposicaoService;
        this.stateService = stateService;
        this.blackboardService = blackboardService;
    }

    public VendaRespostaDTO registrarVenda(
            Long produtoId,
            Long lojaId,
            Integer quantidade) {

        if (quantidade == null || quantidade <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A quantidade da venda deve ser maior que zero");
        }

        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        Loja loja = lojaRepository.findById(lojaId)
                .orElseThrow(() -> new RuntimeException("Loja não encontrada"));

        Estoque estoque = blackboardService.buscarEstoque(produto, loja);

        if (estoque.getQuantidade() < quantidade) {
            throw new RuntimeException("Estoque insuficiente");
        }

        estoque.setQuantidade(estoque.getQuantidade() - quantidade);

        stateService.atualizarEstado(produto, estoque);
        blackboardService.salvarEstoque(estoque);

        Venda venda = new Venda();
        venda.setProduto(produto);
        venda.setLoja(loja);
        venda.setQuantidade(quantidade);
        venda.setDataVenda(LocalDateTime.now());

        Venda vendaSalva = blackboardService.registrarVenda(venda);

        ReposicaoRespostaDTO diagnostico =
                reposicaoService.diagnosticarReposicao(produto, loja);

        if (!"SEM_ACAO".equalsIgnoreCase(diagnostico.tipo())) {
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    reposicaoService.executarReposicao(produto, loja);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (RuntimeException e) {
                    System.err.println("Erro na reposição automática: " + e.getMessage());
                }
            }).start();
        }

        String mensagem = diagnostico.tipo().equalsIgnoreCase("SEM_ACAO")
                ? "Venda registrada. Estoque permanece em zona de conforto."
                : "Venda registrada. Estoque entrou em análise de reposição automática.";

        return new VendaRespostaDTO(
                vendaSalva.getId(),
                produto.getNome(),
                loja.getNome(),
                quantidade,
                estoque.getQuantidade(),
                estoque.getEstado(),
                mensagem,
                diagnostico.tipo(),
                diagnostico.mensagem());
    }
}