package com.mlbcr.projetoaps.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Entity
@Table(name = "funcionarios")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_funcionario")
public abstract class Funcionario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    private String email;

    @ManyToOne
    private Loja loja;

    
    @JsonProperty("tipo_funcionario")
    public String getTipoFuncionario() {
        // Pega o valor definido no @DiscriminatorValue das classes filhas
        // automaticamente, sem precisar de if/else
        jakarta.persistence.DiscriminatorValue dv = this.getClass().getAnnotation(jakarta.persistence.DiscriminatorValue.class);
        return (dv != null) ? dv.value() : "DESCONHECIDO";
    }

}