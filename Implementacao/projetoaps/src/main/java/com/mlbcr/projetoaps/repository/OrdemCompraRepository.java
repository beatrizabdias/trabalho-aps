package com.mlbcr.projetoaps.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mlbcr.projetoaps.model.OrdemCompra;

public interface OrdemCompraRepository
        extends JpaRepository<OrdemCompra, Long> {

}
