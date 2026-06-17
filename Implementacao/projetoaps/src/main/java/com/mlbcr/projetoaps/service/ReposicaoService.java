package com.mlbcr.projetoaps.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mlbcr.projetoaps.model.Estoque;
import com.mlbcr.projetoaps.model.Loja;
import com.mlbcr.projetoaps.model.OrdemCompra;
import com.mlbcr.projetoaps.model.Produto;
import com.mlbcr.projetoaps.model.Transferencia;
import com.mlbcr.projetoaps.observer.ReposicaoEvent;
import com.mlbcr.projetoaps.observer.ReposicaoNotificar;
import com.mlbcr.projetoaps.repository.EstoqueRepository;
import com.mlbcr.projetoaps.repository.LojaRepository;
import com.mlbcr.projetoaps.repository.OrdemCompraRepository;
import com.mlbcr.projetoaps.repository.TransferenciaRepository;
import com.mlbcr.projetoaps.strategy.CompraStrategy;
import com.mlbcr.projetoaps.strategy.TransferenciaStrategy;

@Service
public class ReposicaoService {

    private final EstoqueRepository estoqueRepository;
    private final TransferenciaRepository transferenciaRepository;
    private final OrdemCompraRepository ordemCompraRepository;
    private final LojaRepository lojaRepository;
    private final ReposicaoNotificar reposicaoAcaoNotifier;

    private final TransferenciaStrategy transferenciaStrategy;
    private final CompraStrategy compraStrategy;

    private static final int QTD_TRANSFERENCIA = 15;
    private static final int QTD_COMPRA = 30;

    public ReposicaoService(
            EstoqueRepository estoqueRepository,
            TransferenciaRepository transferenciaRepository,
            OrdemCompraRepository ordemCompraRepository,
            LojaRepository lojaRepository,    
            TransferenciaStrategy transferenciaStrategy,
            CompraStrategy compraStrategy,
            ReposicaoNotificar reposicaoAcaoNotifier) {

        this.estoqueRepository = estoqueRepository;
        this.transferenciaRepository = transferenciaRepository;
        this.ordemCompraRepository = ordemCompraRepository;
        this.lojaRepository = lojaRepository;
        this.transferenciaStrategy = transferenciaStrategy;
        this.compraStrategy = compraStrategy;
        this.reposicaoAcaoNotifier = reposicaoAcaoNotifier;
    } 

    private int obterLimiteMinimo(Loja loja) {
        if (loja != null && "Méier".equalsIgnoreCase(loja.getNome())) {
            return 20; // Margem de segurança maior para o Méier não esgotar
        }
        return 10; // Padrão para as outras lojas
    }

    public void analisarReposicao(Produto produto, Loja loja) {
        Estoque estoqueAtual = estoqueRepository
            .findByProdutoAndLoja(produto, loja)
            .orElseThrow(() -> new RuntimeException("Estoque não encontrado"));

        int limiteCritico = obterLimiteMinimo(loja);
        if (estoqueAtual.getQuantidade() >= limiteCritico) {
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

        if (transferenciaStrategy.podeAplicar(estoqueOutraLoja)) {
            realizarTransferencia(produto, loja, outraLoja, estoqueAtual, estoqueOutraLoja);
            return;
        }

        if (compraStrategy.podeAplicar(estoqueOutraLoja)) {
            gerarOrdemCompra(produto, loja);
        }
    }

    private void realizarTransferencia(
            Produto produto, Loja lojaDestino, Loja lojaOrigem,
            Estoque estoqueDestino, Estoque estoqueOrigem) {

        estoqueDestino.setQuantidade(estoqueDestino.getQuantidade() + QTD_TRANSFERENCIA);
        estoqueOrigem.setQuantidade(estoqueOrigem.getQuantidade() - QTD_TRANSFERENCIA);

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

    @Transactional
    protected void gerarOrdemCompra(Produto produto, Loja loja) {
        OrdemCompra ordemCompra = new OrdemCompra();
        ordemCompra.setProduto(produto);
        ordemCompra.setFornecedor(produto.getFornecedor());
        ordemCompra.setLoja(loja);
        ordemCompra.setQuantidade(QTD_COMPRA);
        ordemCompra.setDataCriacao(LocalDateTime.now());
        ordemCompra.setStatus("PENDENTE");

        OrdemCompra ordemSalva = ordemCompraRepository.save(ordemCompra);

        new Thread(() -> processarCompra(ordemSalva.getId())).start();

        String descricao = String.format(
            "Ordem de compra gerada e concluída automaticamente para a Loja ID %d. Qtd comprada: %d",
            loja.getId(),
            QTD_COMPRA
        );

        ReposicaoEvent event = new ReposicaoEvent(ordemSalva, descricao);
        reposicaoAcaoNotifier.notificar(event);
    }

    private void processarCompra(Long ordemId) {
        try {

            Thread.sleep(3000);

            OrdemCompra ordem = ordemCompraRepository.findById(ordemId)
                    .orElseThrow();

            ordem.setStatus("EM_PROCESSAMENTO");
            ordemCompraRepository.save(ordem);

            Thread.sleep(3000);

            Estoque estoque = estoqueRepository
                    .findByProdutoAndLoja(
                            ordem.getProduto(),
                            ordem.getLoja())
                    .orElseThrow();

            estoque.setQuantidade(
                    estoque.getQuantidade() + ordem.getQuantidade());

            estoqueRepository.save(estoque);

            ordem.setStatus("CONCLUIDA");
            ordemCompraRepository.save(ordem);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}