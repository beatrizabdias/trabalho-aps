package com.mlbcr.projetoaps.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mlbcr.projetoaps.model.Loja;
import com.mlbcr.projetoaps.repository.LojaRepository;

@RestController
@RequestMapping("/lojas")
public class LojaController {
    private final LojaRepository lojaRepository;

    public LojaController(LojaRepository lojaRepository) {
        this.lojaRepository = lojaRepository;
    }

    // GET /lojas
    @GetMapping
    public List<Loja> listarLojas() {
        return lojaRepository.findAll();
    }

    // POST /lojas
    @PostMapping
    public Loja criarLoja(@RequestBody Loja loja) {
        return lojaRepository.save(loja);
    }
}
