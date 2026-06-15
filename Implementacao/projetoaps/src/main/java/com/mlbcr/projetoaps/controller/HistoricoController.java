package com.mlbcr.projetoaps.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mlbcr.projetoaps.model.HistoricoVenda;
import com.mlbcr.projetoaps.model.HistoricoCompra;
import com.mlbcr.projetoaps.service.RelatorioService;

@RestController
@RequestMapping("/historicos")
public class HistoricoController {

    private final RelatorioService relatorioService;

    public HistoricoController(RelatorioService relatorioService) {
        this.relatorioService = relatorioService;
    }

    // Retorna todos os relatórios de vendas consolidados
    @GetMapping("/vendas")
    public List<HistoricoVenda> obterHistoricoVendas() {
        return relatorioService.obterRelatorioVendas();
    }

    // Retorna todos os relatórios de ordens de compras a fornecedores
    @GetMapping("/compras")
    public List<HistoricoCompra> obterHistoricoCompras() {
        return relatorioService.obterRelatorioCompras();
    }
}