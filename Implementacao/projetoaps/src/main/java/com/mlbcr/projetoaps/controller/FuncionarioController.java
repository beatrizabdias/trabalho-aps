package com.mlbcr.projetoaps.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mlbcr.projetoaps.dto.FuncionarioRequest;
import com.mlbcr.projetoaps.model.Funcionario;
import com.mlbcr.projetoaps.service.FuncionarioService;

@RestController
@RequestMapping("/funcionarios")
public class FuncionarioController {

    private final FuncionarioService funcionarioService;

    public FuncionarioController(FuncionarioService funcionarioService) {
        this.funcionarioService = funcionarioService;
    }

    @GetMapping
    public List<Funcionario> listarFuncionarios() {
        return funcionarioService.listarFuncionarios();
    }

    @GetMapping("/email")
    public Funcionario buscarPorEmail(@RequestParam String email) {
        return funcionarioService.buscarPorEmail(email);
    }

    @PostMapping
    public Funcionario criarFuncionario(@RequestBody FuncionarioRequest request) {
        return funcionarioService.criarFuncionario(request);
    }
}