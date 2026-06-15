package com.mlbcr.projetoaps.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
// Siginifica 
@DiscriminatorValue("ATENDENTE")
public class Atendente extends Funcionario {

}