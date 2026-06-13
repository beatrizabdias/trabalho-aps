package com.mlbcr.projetoaps.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mlbcr.projetoaps.model.Transferencia;
import com.mlbcr.projetoaps.repository.TransferenciaRepository;


@RestController
@RequestMapping("/transferencias") // definir a rota base
public class TransferenciaController {
    private final TransferenciaRepository transferenciaRepository;

    // construtor
    public TransferenciaController(TransferenciaRepository transferenciaRepository) {
        this.transferenciaRepository = transferenciaRepository;
    }

    // GET /transferencias
    @GetMapping
    public List<Transferencia> listarTransferencias() {
        return transferenciaRepository.findAll();
    }

    // POST /transferencias
    @PostMapping
    public Transferencia criarTransferencia(@RequestBody Transferencia transferencia) {
        return transferenciaRepository.save(transferencia);
    }
    
}
