package com.mlbcr.projetoaps.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mlbcr.projetoaps.dto.VendaRespostaDTO;
import com.mlbcr.projetoaps.service.VendaService;

@RestController
@RequestMapping("/vendas")
public class VendaController {

    private final VendaService vendaService;

    public VendaController(VendaService vendaService) {
        this.vendaService = vendaService;
    }

    @PostMapping
    public VendaRespostaDTO registrarVenda(
            @RequestParam Long produtoId,
            @RequestParam Long lojaId,
            @RequestParam Integer quantidade) {

        return vendaService.registrarVenda(
                produtoId,
                lojaId,
                quantidade);
    }
}