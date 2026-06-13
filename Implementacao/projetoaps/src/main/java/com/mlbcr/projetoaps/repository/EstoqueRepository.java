package com.mlbcr.projetoaps.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mlbcr.projetoaps.model.Estoque;
import com.mlbcr.projetoaps.model.Loja;
import com.mlbcr.projetoaps.model.Produto;

public interface EstoqueRepository extends JpaRepository<Estoque, Long> {
    // talvez não exista esse estoque, por isso usar optional
    Optional<Estoque> findByProdutoAndLoja(
        Produto produto,
        Loja loja
    );
}