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

        LojaEspecialista especialista = especialistas.stream()
                .filter(e -> e.atendeLoja(lojaId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        "Nenhum especialista encontrado para a loja informada"));

        return especialista.registrarVenda(produtoId, lojaId, quantidade);
    }
}