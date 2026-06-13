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
@Table(name = "vendas")
public class Venda {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // um produto pode estar em várias vendas
    @ManyToOne
    private Produto produto;

    // uma loja pode ter várias vendas
    @ManyToOne
    private Loja loja;

    private Integer quantidade;
    private LocalDateTime dataVenda;
}