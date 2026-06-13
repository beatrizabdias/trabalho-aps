package com.mlbcr.projetoaps.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.mlbcr.projetoaps.model.OrdemCompra;
import com.mlbcr.projetoaps.repository.OrdemCompraRepository;

@RestController
@RequestMapping("/ordens-compra")
public class OrdemCompraController {

    private final OrdemCompraRepository ordemCompraRepository;

    public OrdemCompraController(
            OrdemCompraRepository ordemCompraRepository) {
        this.ordemCompraRepository = ordemCompraRepository;
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
}