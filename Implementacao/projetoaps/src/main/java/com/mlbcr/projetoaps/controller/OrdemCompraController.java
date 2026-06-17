package com.mlbcr.projetoaps.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mlbcr.projetoaps.model.OrdemCompra;
import com.mlbcr.projetoaps.repository.OrdemCompraRepository;
import com.mlbcr.projetoaps.service.OrdemCompraService;

@RestController
@RequestMapping("/ordens-compra")
public class OrdemCompraController {

    private final OrdemCompraRepository ordemCompraRepository;
    private final OrdemCompraService ordemCompraService;

    public OrdemCompraController(
            OrdemCompraRepository ordemCompraRepository,
            OrdemCompraService ordemCompraService) {

        this.ordemCompraRepository = ordemCompraRepository;
        this.ordemCompraService = ordemCompraService;
    }

    @GetMapping
    public List<OrdemCompra> listarOrdensCompra() {
        return ordemCompraRepository.findAll();
    }

    @PostMapping
    public OrdemCompra criarOrdemCompra(
            @RequestBody OrdemCompra ordemCompra) {
        return ordemCompraRepository.save(ordemCompra);
    }
    @PostMapping("/manual")
    public OrdemCompra criarCompraManual(
            @RequestParam Long produtoId,
            @RequestParam Long lojaId,
            @RequestParam Integer quantidade) {

        return ordemCompraService.criarCompraManual(
                produtoId,
                lojaId,
                quantidade);
    }
}