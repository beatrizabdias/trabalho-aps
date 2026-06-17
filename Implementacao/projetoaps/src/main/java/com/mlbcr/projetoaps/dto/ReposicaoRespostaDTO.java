package com.mlbcr.projetoaps.dto;

public record ReposicaoRespostaDTO(
        String tipo,
        String mensagem,
        Integer quantidade,
        Integer estoqueDestino,
        Long lojaOrigemId,
        String lojaOrigemNome) {
}