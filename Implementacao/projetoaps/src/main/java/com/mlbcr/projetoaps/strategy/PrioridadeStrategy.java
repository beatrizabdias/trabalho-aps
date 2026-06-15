package com.mlbcr.projetoaps.strategy;

import com.mlbcr.projetoaps.model.Estoque;
import org.springframework.stereotype.Component;

@Component
public class PrioridadeStrategy {

    // Define o limite mínimo de estoque baseado na loja
    public int obterQuantidadeMinima(Estoque estoque) {
        if (estoque.getLoja() != null && "Méier".equalsIgnoreCase(estoque.getLoja().getNome())) {
            // Política do Méier: Alerta preventivo com 20 unidades (mais alta que o padrão 10)
            return 20; 
        }
        return 10; // Padrão para as demais lojas
    }

    // Define a quantidade a ser comprada/transferida
    public int obterQuantidadeLoteCompra(Estoque estoque) {
        if (estoque.getLoja() != null && "Méier".equalsIgnoreCase(estoque.getLoja().getNome())) {
            // Méier compra em lotes maiores para garantir estoque
            return 50; 
        }
        return 30; // Padrão para as demais lojas
    }
}