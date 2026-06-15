package com.mlbcr.projetoaps.strategy;

import com.mlbcr.projetoaps.model.Produto;
import com.mlbcr.projetoaps.model.Loja;
import org.springframework.stereotype.Component;

@Component
public class DescontoStrategy {

    public Double calcularPrecoComDesconto(Produto produto, Loja loja) {
        Double precoOriginal = produto.getValorVenda();
        
        if (precoOriginal == null) {
            return 0.0;
        }

        if (loja != null && "Tijuca".equalsIgnoreCase(loja.getNome())) {
            // Aplicando política de 15% de desconto para qualquer produto na Tijuca
            return precoOriginal * 0.85;
        }

        return precoOriginal; // Preço normal nas outras lojas
    }
}