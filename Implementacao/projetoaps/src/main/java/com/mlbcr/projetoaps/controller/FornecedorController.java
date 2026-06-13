package com.mlbcr.projetoaps.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mlbcr.projetoaps.model.Fornecedor;
import com.mlbcr.projetoaps.repository.FornecedorRepository;

@RestController
@RequestMapping("/fornecedores")
public class FornecedorController {
    private final FornecedorRepository fornecedorRepository;

    public FornecedorController(
            FornecedorRepository fornecedorRepository) {

        this.fornecedorRepository = fornecedorRepository;
    }

    @GetMapping
    public List<Fornecedor> listarFornecedores() {
        return fornecedorRepository.findAll();
    }

    @PostMapping
    public Fornecedor criarFornecedor(
            @RequestBody Fornecedor fornecedor) {

        return fornecedorRepository.save(fornecedor);
    }
}
