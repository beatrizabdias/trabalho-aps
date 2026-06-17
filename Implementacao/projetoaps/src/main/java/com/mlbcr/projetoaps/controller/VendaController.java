package com.mlbcr.projetoaps.controller;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mlbcr.projetoaps.dto.VendaListDTO;
import com.mlbcr.projetoaps.dto.VendaRespostaDTO;
import com.mlbcr.projetoaps.model.Venda;
import com.mlbcr.projetoaps.repository.VendaRepository;
import com.mlbcr.projetoaps.service.VendaService;

@RestController
@RequestMapping("/vendas")
public class VendaController {

    private final VendaService vendaService;
    private final VendaRepository vendaRepository;

    public VendaController(VendaService vendaService, VendaRepository vendaRepository) {
        this.vendaService = vendaService;
        this.vendaRepository = vendaRepository;
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

    @GetMapping
    public List<Venda> listarVendas() {
        return vendaRepository.findAll();
    }
}