package com.mlbcr.projetoaps.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mlbcr.projetoaps.model.Transferencia;

public interface TransferenciaRepository
        extends JpaRepository<Transferencia, Long> {
}
