package com.mlbcr.projetoaps.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mlbcr.projetoaps.model.Funcionario;
import com.mlbcr.projetoaps.model.Loja;

public interface FuncionarioRepository
        extends JpaRepository<Funcionario, Long> {

    List<Funcionario> findByLoja(Loja loja);

    Optional<Funcionario> findByEmail(String email);
}