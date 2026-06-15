package com.mlbcr.projetoaps.model;

import java.time.LocalDateTime;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "historico_compras")
public class HistoricoCompra {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private OrdemCompra ordemCompra;

    private LocalDateTime dataRegistro;
    private String descricao;

    public HistoricoCompra() {
    }

    public HistoricoCompra(OrdemCompra ordemCompra, String descricao) {
        this.ordemCompra = ordemCompra;
        this.dataRegistro = LocalDateTime.now();
        this.descricao = descricao;
    }
}