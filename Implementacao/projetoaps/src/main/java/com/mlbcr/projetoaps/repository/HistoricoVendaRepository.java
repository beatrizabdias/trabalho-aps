package com.mlbcr.projetoaps.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.mlbcr.projetoaps.model.HistoricoVenda;

public interface HistoricoVendaRepository extends JpaRepository<HistoricoVenda, Long> {
    // Caso precise no futuro, métodos de busca por período podem ser adicionados aqui
}