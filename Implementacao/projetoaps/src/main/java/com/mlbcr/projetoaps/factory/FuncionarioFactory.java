package com.mlbcr.projetoaps.factory;

import org.springframework.stereotype.Component;

import com.mlbcr.projetoaps.dto.FuncionarioRequest;
import com.mlbcr.projetoaps.model.Admin;
import com.mlbcr.projetoaps.model.Atendente;
import com.mlbcr.projetoaps.model.Funcionario;
import com.mlbcr.projetoaps.model.Gerente;
import com.mlbcr.projetoaps.model.Loja;

@Component
public class FuncionarioFactory {

    public Funcionario criarFuncionario(
            FuncionarioRequest request,
            Loja loja) {

        Funcionario funcionario;

        if ("GERENTE".equalsIgnoreCase(request.getTipo())) {
            funcionario = new Gerente();
        } else if ("ATENDENTE".equalsIgnoreCase(request.getTipo())) {
            funcionario = new Atendente();
        } else if ("ADMIN".equalsIgnoreCase(request.getTipo())) {
            funcionario = new Admin();
        } else {
            throw new RuntimeException("Tipo de funcionário inválido");
        }

        funcionario.setNome(request.getNome());
        funcionario.setEmail(request.getEmail());
        funcionario.setLoja(loja);

        return funcionario;
    }
}