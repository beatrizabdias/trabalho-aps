package com.mlbcr.projetoaps.observer;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ReposicaoNotificar {

    private final List<ReposicaoAcaoListener> listeners = new ArrayList<>();

    // O Spring injeta automaticamente todos os beans do projeto que implementam ReposicaoAcaoListener
    public ReposicaoNotificar(List<ReposicaoAcaoListener> listeners) {
        if (listeners != null) {
            this.listeners.addAll(listeners);
        }
    }

    public void registrarListener(ReposicaoAcaoListener listener) {
        this.listeners.add(listener);
    }

    public void notificar(ReposicaoEvent event) {
        for (ReposicaoAcaoListener listener : listeners) {
            listener.onAcaoReposicao(event);
        }
    }
}