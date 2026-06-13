package com.mlbcr.projetoaps.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.mlbcr.projetoaps.model.Estoque;
import com.mlbcr.projetoaps.model.Loja;
import com.mlbcr.projetoaps.model.OrdemCompra;
import com.mlbcr.projetoaps.model.Produto;
import com.mlbcr.projetoaps.model.Transferencia;
import com.mlbcr.projetoaps.repository.EstoqueRepository;
import com.mlbcr.projetoaps.repository.LojaRepository;
import com.mlbcr.projetoaps.repository.OrdemCompraRepository;
import com.mlbcr.projetoaps.repository.TransferenciaRepository;

@Service
public class ReposicaoService {

    private final EstoqueRepository estoqueRepository;
    private final TransferenciaRepository transferenciaRepository;
    private final OrdemCompraRepository ordemCompraRepository;
    private final LojaRepository lojaRepository;

    private static final int MIN_QTD = 10;
    private static final int ESTOQUE_TRANSFERENCIA = 25;
    private static final int QTD_TRANSFERENCIA = 15;
    private static final int QTD_COMPRA = 30;

    public ReposicaoService(
            EstoqueRepository estoqueRepository,
            TransferenciaRepository transferenciaRepository,
            OrdemCompraRepository ordemCompraRepository,
            LojaRepository lojaRepository) {

        this.estoqueRepository = estoqueRepository;
        this.transferenciaRepository = transferenciaRepository;
        this.ordemCompraRepository = ordemCompraRepository;
        this.lojaRepository = lojaRepository;
    }

    public void analisarReposicao(Produto produto, Loja loja) {

        Estoque estoqueAtual = estoqueRepository
                .findByProdutoAndLoja(produto, loja)
                .orElseThrow(() -> new RuntimeException("Estoque não encontrado"));

        if (estoqueAtual.getQuantidade() >= MIN_QTD) {
            return;
        }

        Loja outraLoja = lojaRepository.findAll()
                .stream()
                .filter(l -> !l.getId().equals(loja.getId()))
                .findFirst()
                .orElse(null);

        if (outraLoja == null) {
            gerarOrdemCompra(produto, loja);
            return;
        }

        Estoque estoqueOutraLoja = estoqueRepository
                .findByProdutoAndLoja(produto, outraLoja)
                .orElse(null);

        if (estoqueOutraLoja != null &&
                estoqueOutraLoja.getQuantidade() > ESTOQUE_TRANSFERENCIA) {

            realizarTransferencia(produto, loja, outraLoja, estoqueAtual, estoqueOutraLoja);
            return;
        }

        gerarOrdemCompra(produto, loja);
    }

    private void realizarTransferencia(
            Produto produto,
            Loja lojaDestino,
            Loja lojaOrigem,
            Estoque estoqueDestino,
            Estoque estoqueOrigem) {

        estoqueDestino.setQuantidade(
                estoqueDestino.getQuantidade() + QTD_TRANSFERENCIA
        );

        estoqueOrigem.setQuantidade(
                estoqueOrigem.getQuantidade() - QTD_TRANSFERENCIA
        );

        estoqueRepository.save(estoqueDestino);
        estoqueRepository.save(estoqueOrigem);

        Transferencia transferencia = new Transferencia();
        transferencia.setProduto(produto);
        transferencia.setLojaOrigem(lojaOrigem);
        transferencia.setLojaDestino(lojaDestino);
        transferencia.setQuantidade(QTD_TRANSFERENCIA);
        transferencia.setDataTransferencia(LocalDateTime.now());

        transferenciaRepository.save(transferencia);
    }

    private void gerarOrdemCompra(Produto produto, Loja loja) {

        OrdemCompra ordemCompra = new OrdemCompra();
        ordemCompra.setProduto(produto);
        ordemCompra.setFornecedor(produto.getFornecedor());
        ordemCompra.setLoja(loja);
        ordemCompra.setQuantidade(QTD_COMPRA);
        ordemCompra.setDataCriacao(LocalDateTime.now());
        ordemCompra.setStatus("PENDENTE");

        ordemCompraRepository.save(ordemCompra);
    }
}