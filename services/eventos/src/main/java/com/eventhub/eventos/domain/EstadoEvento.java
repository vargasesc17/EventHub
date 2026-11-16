package com.eventhub.eventos.domain;

public enum EstadoEvento {
    BORRADOR,
    PUBLICADO,
    EN_CURSO,
    FINALIZADO,
    CANCELADO;

    /** Las únicas transiciones permitidas (RF-02). Cualquier otra se rechaza. */
    public boolean puedePasarA(EstadoEvento destino) {
        return switch (this) {
            case BORRADOR -> destino == PUBLICADO || destino == CANCELADO;
            case PUBLICADO -> destino == EN_CURSO || destino == CANCELADO;
            case EN_CURSO -> destino == FINALIZADO;
            case FINALIZADO, CANCELADO -> false;
        };
    }
}