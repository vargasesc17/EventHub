package com.eventhub.boleteria.domain;

public abstract class FabricaBoleta {

    /** Qué tipo de boleta sabe crear esta fábrica. */
    public abstract TipoBoleta tipo();

    /** Lo que usa la compra. Es final: las subclases no pueden cambiar este flujo. */
    public final Boleta emitir(Compra compra, long precio) {
        if (compra.categoria() != tipo()) {
            throw new ReglaDeNegocioException(
                    "La fábrica de " + tipo() + " no puede emitir una boleta " + compra.categoria());
        }
        return crearBoleta(compra, precio);
    }

    /** El "factory method": cada subclase decide qué clase concreta crear. */
    protected abstract Boleta crearBoleta(Compra compra, long precio);
}