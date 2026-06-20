package com.mlbcr.projetoaps.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.mlbcr.projetoaps.dto.VendaRespostaDTO;
import com.mlbcr.projetoaps.especialista.LojaEspecialista;

@Service
public class VendaService {

    private final List<LojaEspecialista> especialistas;

    public VendaService(List<LojaEspecialista> especialistas) {
        this.especialistas = especialistas;
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

        Produto produto = produtoRepository
                .findById(produtoId)
                .orElseThrow(
                        () -> new RuntimeException("Produto não encontrado"));

        Loja loja = lojaRepository
                .findById(lojaId)
                .orElseThrow(
                        () -> new RuntimeException("Loja não encontrada"));

        Estoque estoque = estoqueRepository
                .findByProdutoAndLoja(produto, loja)
                .orElseThrow(
                        () -> new RuntimeException("Estoque não encontrado"));

        if (estoque.getQuantidade() < quantidade) {
            throw new RuntimeException("Estoque insuficiente");
        }
        estoque.setQuantidade(
                estoque.getQuantidade() - quantidade);
        stateService.atualizarEstado(produto, estoque);
        estoqueRepository.save(estoque);

        Venda venda = new Venda();

        venda.setProduto(produto);
        venda.setLoja(loja);
        venda.setQuantidade(quantidade);
        venda.setDataVenda(LocalDateTime.now());

        Venda vendaSalva = vendaRepository.save(venda);

        ReposicaoRespostaDTO diagnostico = reposicaoService.diagnosticarReposicao(produto, loja);

        if (!"SEM_ACAO".equalsIgnoreCase(diagnostico.tipo())) {
            new Thread(() -> {
                try {
                    Thread.sleep(2000);

                    Produto produtoRecarregado = produtoRepository
                            .findById(produtoId)
                            .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
                    Loja lojaRecarregada = lojaRepository
                            .findById(lojaId)
                            .orElseThrow(() -> new RuntimeException("Loja não encontrada"));

                    reposicaoService.executarReposicao(produtoRecarregado, lojaRecarregada);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (RuntimeException e) {
                    System.err.println("Erro na reposição automática: " + e.getMessage());
                }
            }).start();
        }

        String mensagem = diagnostico.tipo().equalsIgnoreCase("SEM_ACAO")
                ? "Venda registrada."
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