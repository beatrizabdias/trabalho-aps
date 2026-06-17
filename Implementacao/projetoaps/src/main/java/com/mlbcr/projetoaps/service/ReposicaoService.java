package com.mlbcr.projetoaps.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mlbcr.projetoaps.dto.ReposicaoRespostaDTO;
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

    private static final int LIMITE_CRITICO = 20;

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
        executarReposicao(produto, loja);
    }

    public ReposicaoRespostaDTO diagnosticarReposicao(Produto produto, Loja loja) {
        return resolverReposicao(produto, loja, false);
    }

    public ReposicaoRespostaDTO executarReposicao(Produto produto, Loja loja) {
        return resolverReposicao(produto, loja, true);
    }

    private ReposicaoRespostaDTO resolverReposicao(Produto produto, Loja loja, boolean executar) {
        Estoque estoqueAtual = estoqueRepository
                .findByProdutoAndLoja(produto, loja)
                .orElseThrow(() -> new RuntimeException("Estoque não encontrado"));

        int quantidadeAtual = estoqueAtual.getQuantidade() == null ? 0 : estoqueAtual.getQuantidade();

        if (quantidadeAtual == 0) {
            int quantidadeCompra = prioridadeStrategy.calcularQuantidadeCompra(estoqueAtual);
            if (executar) {
                gerarOrdemCompra(produto, loja, quantidadeCompra);
            }

            return new ReposicaoRespostaDTO(
                    "COMPRA",
                    String.format("Estoque esgotado. Ordem de compra para atingir %d unidades.",
                            prioridadeStrategy.obterMetaCompra()),
                    quantidadeCompra,
                    prioridadeStrategy.obterMetaCompra(),
                    null,
                    null);
        }

        if (quantidadeAtual <= LIMITE_CRITICO) {
            int quantidadeNecessaria = prioridadeStrategy.obterMetaTransferencia() - quantidadeAtual;

            if (quantidadeNecessaria > 0) {
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

                    int saldoDisponivel = estoqueOutraLoja.getQuantidade()
                            - prioridadeStrategy.obterLimiteSeguroOrigem();

                    if (saldoDisponivel >= quantidadeNecessaria && saldoDisponivel > maiorSaldoDisponivel) {
                        maiorSaldoDisponivel = saldoDisponivel;
                        melhorLojaOrigem = outraLoja;
                        melhorEstoqueOrigem = estoqueOutraLoja;
                    }
                }

                if (melhorEstoqueOrigem != null && transferenciaStrategy.podeAplicar(melhorEstoqueOrigem)) {
                    if (executar) {
                        realizarTransferencia(produto, loja, melhorLojaOrigem, estoqueAtual, melhorEstoqueOrigem,
                                quantidadeNecessaria);
                    }

                    return new ReposicaoRespostaDTO(
                            "TRANSFERENCIA",
                            String.format("Reposição por transferência para atingir %d unidades.",
                                    prioridadeStrategy.obterMetaTransferencia()),
                            quantidadeNecessaria,
                            prioridadeStrategy.obterMetaTransferencia(),
                            melhorLojaOrigem.getId(),
                            melhorLojaOrigem.getNome());
                }
            }

            int quantidadeCompra = prioridadeStrategy.calcularQuantidadeCompra(estoqueAtual);
            if (executar) {
                gerarOrdemCompra(produto, loja, quantidadeCompra);
            }

            return new ReposicaoRespostaDTO(
                    "COMPRA",
                    String.format("Transferência insuficiente. Ordem de compra para atingir %d unidades.",
                            prioridadeStrategy.obterMetaCompra()),
                    quantidadeCompra,
                    prioridadeStrategy.obterMetaCompra(),
                    null,
                    null);
        }

        return new ReposicaoRespostaDTO(
                "SEM_ACAO",
                "Estoque já está em zona de conforto. Nenhuma reposição automática necessária.",
                0,
                quantidadeAtual,
                null,
                null);
    }

    private void realizarTransferencia(
            Produto produto, Loja lojaDestino, Loja lojaOrigem,
            Estoque estoqueDestino, Estoque estoqueOrigem, int quantidadeTransferencia) {

        int maxTransferivel = estoqueOrigem.getQuantidade() - prioridadeStrategy.obterLimiteSeguroOrigem();
        int quantidadeEfetiva = Math.min(quantidadeTransferencia, Math.max(maxTransferivel, 0));

        if (quantidadeEfetiva <= 0) {
            return;
        }

        estoqueDestino.setQuantidade(estoqueDestino.getQuantidade() + quantidadeEfetiva);
        estoqueOrigem.setQuantidade(estoqueOrigem.getQuantidade() - quantidadeEfetiva);

        stateService.atualizarEstado(produto, estoqueDestino);
        stateService.atualizarEstado(produto, estoqueOrigem);

        estoqueRepository.save(estoqueDestino);
        estoqueRepository.save(estoqueOrigem);

        Transferencia transferencia = new Transferencia();
        transferencia.setProduto(produto);
        transferencia.setLojaOrigem(lojaOrigem);
        transferencia.setLojaDestino(lojaDestino);
        transferencia.setQuantidade(quantidadeEfetiva);
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
        int quantidadeCompra = prioridadeStrategy.calcularQuantidadeCompra(
                estoqueRepository.findByProdutoAndLoja(produto, loja)
                        .orElseThrow(() -> new RuntimeException("Estoque não encontrado")));
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
                    ordemConcluida.getQuantidade());

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