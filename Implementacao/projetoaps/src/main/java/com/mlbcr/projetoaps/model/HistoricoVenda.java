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
@Table(name = "historico_vendas")
public class HistoricoVenda {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Vincula o histórico à venda correspondente
    @ManyToOne
    private Venda venda;

    private LocalDateTime dataRegistro;
    private String descricao;

    // Construtor padrão necessário para o JPA
    public HistoricoVenda() {
    }

    // Construtor auxiliar para facilitar a criação no Service
    public HistoricoVenda(Venda venda, String descricao) {
        this.venda = venda;
        this.dataRegistro = LocalDateTime.now();
        this.descricao = descricao;
    }
}