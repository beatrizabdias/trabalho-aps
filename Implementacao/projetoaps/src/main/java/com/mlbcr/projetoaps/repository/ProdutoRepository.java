package com.mlbcr.projetoaps.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mlbcr.projetoaps.model.Produto;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    
}
