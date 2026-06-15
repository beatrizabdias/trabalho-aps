package com.mlbcr.projetoaps.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponse {

    private Long id;
    private String nome;
    private String email;
    private String tipo;
    private Long lojaId;
    private String lojaNome;
}