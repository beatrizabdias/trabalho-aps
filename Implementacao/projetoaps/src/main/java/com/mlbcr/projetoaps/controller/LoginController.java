package com.mlbcr.projetoaps.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mlbcr.projetoaps.dto.LoginRequest;
import com.mlbcr.projetoaps.dto.LoginResponse;
import com.mlbcr.projetoaps.model.Atendente;
import com.mlbcr.projetoaps.model.Funcionario;
import com.mlbcr.projetoaps.model.Gerente;
import com.mlbcr.projetoaps.repository.FuncionarioRepository;

@RestController
@RequestMapping("/login")
public class LoginController {

    private final FuncionarioRepository funcionarioRepository;

    public LoginController(FuncionarioRepository funcionarioRepository) {
        this.funcionarioRepository = funcionarioRepository;
    }

    @PostMapping
    public LoginResponse login(@RequestBody LoginRequest request) {

        Funcionario funcionario = funcionarioRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Funcionário não encontrado"));

        LoginResponse response = new LoginResponse();
        response.setId(funcionario.getId());
        response.setNome(funcionario.getNome());
        response.setEmail(funcionario.getEmail());

        if (funcionario instanceof Gerente) {
            response.setTipo("GERENTE");
        } else if (funcionario instanceof Atendente) {
            response.setTipo("ATENDENTE");
        } else {
            response.setTipo("FUNCIONARIO");
        }

        if (funcionario.getLoja() != null) {
            response.setLojaId(funcionario.getLoja().getId());
            response.setLojaNome(funcionario.getLoja().getNome());
        }

        return response;
    }
}