package com.mlbcr.projetoaps.dto;

public record VendaListDTO(
        Long id,
        String produto,
        String loja,
        Integer quantidade,
        String dataVenda
) {
}
