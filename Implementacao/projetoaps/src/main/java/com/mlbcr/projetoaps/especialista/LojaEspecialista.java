package com.mlbcr.projetoaps.especialista;

import com.mlbcr.projetoaps.dto.VendaRespostaDTO;

public interface LojaEspecialista {

    Long getLojaId();

    String getNomeLoja();

    boolean atendeLoja(Long lojaId);

    VendaRespostaDTO registrarVenda(Long produtoId, Long lojaId, Integer quantidade);
}