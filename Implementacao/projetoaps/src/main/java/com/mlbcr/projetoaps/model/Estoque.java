package com.mlbcr.projetoaps.model;

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
@Table(name="estoques")
public class Estoque {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    // muitos estoques podem apontar pra um produto
    @ManyToOne
    private Produto produto;

    // muitas estoques podem apontar pra uma loja
    @ManyToOne
    private Loja loja;

    private Integer quantidade;
}

