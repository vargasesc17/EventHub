package com.eventhub.boleteria.domain;

public class PrecioDinamico implements PrecioStrategy {

    @Override
    public TipoPrecio tipo() { return TipoPrecio.DINAMICO; }

    /**
     * Sube en proporción a la ocupación: con 0 vendidas cuesta el precio base y con todo
     * vendido cuesta base + incrementoMaximo %. Todo en enteros para no perder centavos.
     */
    @Override
    public long calcular(ContextoPrecio contexto) {
        Integer incremento = contexto.parametros().incrementoMaximoPorcentaje();
        if (incremento == null || incremento <= 0) {
            throw new ReglaDeNegocioException("El precio dinámico exige un incremento máximo mayor que 0");
        }
        long cupos = contexto.cupos();
        return contexto.precioBase() * (100 * cupos + (long) incremento * contexto.vendidas()) / (100 * cupos);
    }
}