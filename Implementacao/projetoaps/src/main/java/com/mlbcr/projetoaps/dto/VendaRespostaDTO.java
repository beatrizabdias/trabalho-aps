package com.mlbcr.projetoaps.dto;

public record VendaRespostaDTO(
        Long vendaId,
        String produto,
        String loja,
        Integer quantidadeVendida,
        Integer estoqueAtual,
        String estadoEstoque,
        String mensagem,
        String tipoReposicao,
        String detalheReposicao) {
}