package com.example.cariocada.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.cariocada.repository.CompraRepository;
import com.example.cariocada.repository.EstoqueRepository;
import com.example.cariocada.repository.VendaRepository;

@Component
public class Blackboard {

    @Autowired
    private EstoqueRepository estoqueRepository;

    @Autowired
    private VendaRepository vendaRepository;

    @Autowired
    private CompraRepository compraRepository;

    // listas apenas pra alertas e logs em memória
    private final List<String> alertasEReposicoes = new ArrayList<>();

    public List<Estoque> getEstoqueLojas() {
        try {
            List<Estoque> lista = estoqueRepository.findAll();
            if (lista == null) {
                return new ArrayList<>();
            }
            return lista;
        } catch (Exception e) {
            System.out.println("[BLACKBOARD ERROR] Erro ao ler estoque do banco: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // 2. MÉTODOS DE ALERTAS (Controle de Quadro)
    public List<String> getAlertasEReposicoes() {
        return this.alertasEReposicoes;
    }

    public void adicionarAlerta(String alerta) {
        if (alerta != null && !this.alertasEReposicoes.contains(alerta)) {
            this.alertasEReposicoes.add(alerta);
        }
    }

    public void limparAlertas() {
        this.alertasEReposicoes.clear();
    }

    // 3. PERSISTÊNCIA REAL DE VENDAS E COMPRAS
    public void salvarVenda(Venda venda) {
        try {
            vendaRepository.save(venda);
        } catch (Exception e) {
            System.out.println("[BLACKBOARD ERROR] Erro ao salvar venda: " + e.getMessage());
        }
    }

    public void salvarCompra(Compra compra) {
        try {
            compraRepository.save(compra);
        } catch (Exception e) {
            System.out.println("[BLACKBOARD ERROR] Erro ao salvar compra: " + e.getMessage());
        }
    }

    // Métodos extras para o Controller não quebrar caso use
    public List<Venda> getHistoricoVendas() {
        return vendaRepository.findAll();
    }

    public List<Compra> getHistoricoComprasFornecedores() {
        return compraRepository.findAll();
    }

    public List<Fornecedor> getFornecedores() {
        // Se você tiver um FornecedorRepository, use-o aqui. Caso contrário, retorne uma lista vazia segura
        return new ArrayList<>(); 
    }
}