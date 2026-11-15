package com.eventhub.eventos.infrastructure;

import com.eventhub.eventos.domain.TipoBoleta;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
public class CategoriaBoletaEmbeddable {

    @Enumerated(EnumType.STRING)
    private TipoBoleta tipo;
    private int cupos;
    private long precioBase;

    protected CategoriaBoletaEmbeddable() { }   // lo exige JPA

    public CategoriaBoletaEmbeddable(TipoBoleta tipo, int cupos, long precioBase) {
        this.tipo = tipo;
        this.cupos = cupos;
        this.precioBase = precioBase;
    }

    public TipoBoleta getTipo() { return tipo; }
    public int getCupos() { return cupos; }
    public long getPrecioBase() { return precioBase; }
}