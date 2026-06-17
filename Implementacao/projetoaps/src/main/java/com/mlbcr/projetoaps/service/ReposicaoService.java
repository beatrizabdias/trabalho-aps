package com.mlbcr.projetoaps.service;

import java.time.LocalDateTime;
import java.util.List;

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
import com.mlbcr.projetoaps.strategy.PrioridadeStrategy;
import com.mlbcr.projetoaps.strategy.TransferenciaStrategy;

@Service
public class ReposicaoService {

    private final EstoqueRepository estoqueRepository;
    private final TransferenciaRepository transferenciaRepository;
    private final OrdemCompraRepository ordemCompraRepository;
    private final LojaRepository lojaRepository;
    private final ReposicaoNotificar reposicaoAcaoNotifier;

    private final TransferenciaStrategy transferenciaStrategy;
    private final PrioridadeStrategy prioridadeStrategy;

    private final StateService stateService;

    private static final int QTD_TRANSFERENCIA = 15;

    public ReposicaoService(
            EstoqueRepository estoqueRepository,
            TransferenciaRepository transferenciaRepository,
            OrdemCompraRepository ordemCompraRepository,
            LojaRepository lojaRepository,
            TransferenciaStrategy transferenciaStrategy,
            PrioridadeStrategy prioridadeStrategy,
            ReposicaoNotificar reposicaoAcaoNotifier,
            StateService stateService) {

        this.estoqueRepository = estoqueRepository;
        this.transferenciaRepository = transferenciaRepository;
        this.ordemCompraRepository = ordemCompraRepository;
        this.lojaRepository = lojaRepository;
        this.transferenciaStrategy = transferenciaStrategy;
        this.prioridadeStrategy = prioridadeStrategy;
        this.reposicaoAcaoNotifier = reposicaoAcaoNotifier;
        this.stateService = stateService;
    } 

    public void analisarReposicao(Produto produto, Loja loja) {
        Estoque estoqueAtual = estoqueRepository
            .findByProdutoAndLoja(produto, loja)
            .orElseThrow(() -> new RuntimeException("Estoque não encontrado"));

        int limiteCritico = prioridadeStrategy.obterQuantidadeMinima(estoqueAtual);
        if (estoqueAtual.getQuantidade() >= limiteCritico) {
            return;
        }

        List<Loja> outrasLojas = lojaRepository.findAll()
                .stream()
                .filter(l -> !l.getId().equals(loja.getId()))
                .toList();

        Estoque melhorEstoqueOrigem = null;
        Loja melhorLojaOrigem = null;
        int maiorSaldoDisponivel = 0;

        for (Loja outraLoja : outrasLojas) {
            Estoque estoqueOutraLoja = estoqueRepository
                    .findByProdutoAndLoja(produto, outraLoja)
                    .orElse(null);

            if (estoqueOutraLoja == null) {
                continue;
            }

            int limiteOrigem = prioridadeStrategy.obterQuantidadeMinima(estoqueOutraLoja);
            int saldoDisponivel = estoqueOutraLoja.getQuantidade() - limiteOrigem;

            if (saldoDisponivel > maiorSaldoDisponivel) {
                maiorSaldoDisponivel = saldoDisponivel;
                melhorLojaOrigem = outraLoja;
                melhorEstoqueOrigem = estoqueOutraLoja;
            }
        }

        if (melhorEstoqueOrigem != null && transferenciaStrategy.podeAplicar(melhorEstoqueOrigem)) {
            realizarTransferencia(produto, loja, melhorLojaOrigem, estoqueAtual, melhorEstoqueOrigem);
            return;
        }

        int quantidadeCompra = prioridadeStrategy.obterQuantidadeLoteCompra(estoqueAtual);
        gerarOrdemCompra(produto, loja, quantidadeCompra);
    }

    private void realizarTransferencia(
            Produto produto, Loja lojaDestino, Loja lojaOrigem,
            Estoque estoqueDestino, Estoque estoqueOrigem) {

        int limiteOrigem = prioridadeStrategy.obterQuantidadeMinima(estoqueOrigem);
        int maxTransferivel = estoqueOrigem.getQuantidade() - limiteOrigem;
        int quantidadeTransferencia = Math.min(QTD_TRANSFERENCIA, Math.max(maxTransferivel, 0));

        if (quantidadeTransferencia <= 0) {
            return;
        }

        estoqueDestino.setQuantidade(estoqueDestino.getQuantidade() + quantidadeTransferencia);
        estoqueOrigem.setQuantidade(estoqueOrigem.getQuantidade() - quantidadeTransferencia);

        stateService.atualizarEstado(produto, estoqueDestino);
        stateService.atualizarEstado(produto, estoqueOrigem);

        estoqueRepository.save(estoqueDestino);
        estoqueRepository.save(estoqueOrigem);

        Transferencia transferencia = new Transferencia();
        transferencia.setProduto(produto);
        transferencia.setLojaOrigem(lojaOrigem);
        transferencia.setLojaDestino(lojaDestino);
        transferencia.setQuantidade(quantidadeTransferencia);
        transferencia.setDataTransferencia(LocalDateTime.now());

        transferenciaRepository.save(transferencia);
    }

    @Transactional
    protected void gerarOrdemCompra(Produto produto, Loja loja, int quantidade) {
        OrdemCompra ordemCompra = new OrdemCompra();
        ordemCompra.setProduto(produto);
        ordemCompra.setFornecedor(produto.getFornecedor());
        ordemCompra.setLoja(loja);
        ordemCompra.setQuantidade(quantidade);
        ordemCompra.setDataCriacao(LocalDateTime.now());
        ordemCompra.setStatus("PENDENTE");

        OrdemCompra ordemSalva = ordemCompraRepository.save(ordemCompra);

        new Thread(() -> processarCompra(ordemSalva.getId())).start();
    }

    @Transactional
    protected void gerarOrdemCompra(Produto produto, Loja loja) {
        int quantidadeCompra = prioridadeStrategy.obterQuantidadeLoteCompra(
            estoqueRepository.findByProdutoAndLoja(produto, loja)
                .orElseThrow(() -> new RuntimeException("Estoque não encontrado"))
        );
        gerarOrdemCompra(produto, loja, quantidadeCompra);
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

            stateService.atualizarEstado(ordem.getProduto(), estoque);

            estoqueRepository.save(estoque);

            ordem.setStatus("CONCLUIDA");
            OrdemCompra ordemConcluida = ordemCompraRepository.save(ordem);

            String descricao = String.format(
                "Ordem de compra concluída automaticamente para a Loja ID %d. Qtd comprada: %d",
                ordemConcluida.getLoja().getId(),
                ordemConcluida.getQuantidade()
            );

            ReposicaoEvent event = new ReposicaoEvent(ordemConcluida, descricao);
            reposicaoAcaoNotifier.notificar(event);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("ReposicaoService interrompido: " + e.getMessage());
        } catch (RuntimeException e) {
            System.err.println("Erro ao processar ordem de compra: " + e.getMessage());
        }
    }
}