package com.mlbcr.projetoaps.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.mlbcr.projetoaps.model.HistoricoCompra;

public interface HistoricoCompraRepository extends JpaRepository<HistoricoCompra, Long> {
}