package com.mlbcr.projetoaps.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mlbcr.projetoaps.model.Estoque;
import com.mlbcr.projetoaps.repository.EstoqueRepository;


@RestController
@RequestMapping("/estoques")
public class EstoqueController {
    private final EstoqueRepository estoqueRepository;

    public EstoqueController(EstoqueRepository estoqueRepository) {
        this.estoqueRepository = estoqueRepository;
    }

    @GetMapping
    public List<Estoque> listarEstoques() {
        return estoqueRepository.findAll();
    }

    @PostMapping
    public Estoque criarEstoque(@RequestBody Estoque estoque) {
        return estoqueRepository.save(estoque);
    }
}
