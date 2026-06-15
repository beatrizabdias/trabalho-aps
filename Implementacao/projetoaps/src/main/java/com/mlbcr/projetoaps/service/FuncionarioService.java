package com.mlbcr.projetoaps.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.mlbcr.projetoaps.dto.FuncionarioRequest;
import com.mlbcr.projetoaps.factory.FuncionarioFactory;
import com.mlbcr.projetoaps.model.Funcionario;
import com.mlbcr.projetoaps.model.Gerente;
import com.mlbcr.projetoaps.model.Loja;
import com.mlbcr.projetoaps.repository.FuncionarioRepository;
import com.mlbcr.projetoaps.repository.LojaRepository;

@Service
public class FuncionarioService {

    private final FuncionarioRepository funcionarioRepository;
    private final LojaRepository lojaRepository;
    private final FuncionarioFactory funcionarioFactory;

    public FuncionarioService(
            FuncionarioRepository funcionarioRepository,
            LojaRepository lojaRepository,
            FuncionarioFactory funcionarioFactory) {

        this.funcionarioRepository = funcionarioRepository;
        this.lojaRepository = lojaRepository;
        this.funcionarioFactory = funcionarioFactory;
    }

    public List<Funcionario> listarFuncionarios() {
        return funcionarioRepository.findAll();
    }

    public Funcionario criarFuncionario(FuncionarioRequest request) {

        Loja loja = lojaRepository.findById(request.getLojaId())
                .orElseThrow(() -> new RuntimeException("Loja não encontrada"));

        Funcionario funcionario = funcionarioFactory.criarFuncionario(request, loja);

        if (funcionario instanceof Gerente) {
            boolean jaExisteGerenteNaLoja = funcionarioRepository
                    .findByLoja(loja)
                    .stream()
                    .anyMatch(f -> f instanceof Gerente);

            if (jaExisteGerenteNaLoja) {
                throw new RuntimeException("Esta loja já possui um gerente cadastrado");
            }
        }

        return funcionarioRepository.save(funcionario);
    }
    public Funcionario buscarPorEmail(String email) {
        return funcionarioRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Funcionário não encontrado"));
    }
}