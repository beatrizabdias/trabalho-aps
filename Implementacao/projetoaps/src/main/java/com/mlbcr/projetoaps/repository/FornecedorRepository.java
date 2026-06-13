package com.mlbcr.projetoaps.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mlbcr.projetoaps.model.Fornecedor;

public interface FornecedorRepository
        extends JpaRepository<Fornecedor, Long> {

}