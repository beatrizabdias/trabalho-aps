package com.mlbcr.projetoaps.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mlbcr.projetoaps.model.Venda;
import com.mlbcr.projetoaps.model.HistoricoVenda;
import com.mlbcr.projetoaps.model.OrdemCompra;
import com.mlbcr.projetoaps.model.HistoricoCompra;

import com.mlbcr.projetoaps.repository.HistoricoVendaRepository;
import com.mlbcr.projetoaps.repository.HistoricoCompraRepository;

@Service
public class RelatorioService {

    private final HistoricoVendaRepository historicoVendaRepository;
    private final HistoricoCompraRepository historicoCompraRepository;

    public RelatorioService(
            HistoricoVendaRepository historicoVendaRepository,
            HistoricoCompraRepository historicoCompraRepository) {
        this.historicoVendaRepository = historicoVendaRepository;
        this.historicoCompraRepository = historicoCompraRepository;
    }

    @Transactional
    public void registrarHistoricoVenda(Venda venda, String descricao) {
        HistoricoVenda historico = new HistoricoVenda(venda, descricao);
        historicoVendaRepository.save(historico);
    }

    @Transactional
    public void registrarHistoricoCompra(OrdemCompra ordemCompra, String descricao) {
        HistoricoCompra historico = new HistoricoCompra(ordemCompra, descricao);
        historicoCompraRepository.save(historico);
    }

    public List<HistoricoVenda> obterRelatorioVendas() {
        return historicoVendaRepository.findAll();
    }

    public List<HistoricoCompra> obterRelatorioCompras() {
        return historicoCompraRepository.findAll();
    }
}