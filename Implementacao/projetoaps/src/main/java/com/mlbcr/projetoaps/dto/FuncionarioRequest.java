package com.mlbcr.projetoaps.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FuncionarioRequest {

    private String tipo;
    private String nome;
    private String email;
    private Long lojaId;
}