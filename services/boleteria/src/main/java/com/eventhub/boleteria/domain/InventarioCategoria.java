package com.eventhub.boleteria.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Foto del inventario de una categoría de un evento (ADR-0003). Los contadores no se cambian
 * aquí: se cambian en la base con actualizaciones atómicas, para no vender de más.
 */
public record InventarioCategoria(UUID eventoId, TipoBoleta categoria, int cupos, int disponibles,
                                  int reservadas, int vendidas, long precioBase,
                                  TipoPrecio tipoPrecio, ParametrosPrecio parametrosPrecio) {

    public InventarioCategoria {
        if (eventoId == null || categoria == null) {
            throw new ReglaDeNegocioException("El inventario exige evento y categoría");
        }
        if (cupos < 0 || disponibles < 0 || reservadas < 0 || vendidas < 0) {
            throw new ReglaDeNegocioException("Los contadores del inventario no pueden ser negativos");
        }
        if (disponibles + reservadas + vendidas != cupos) {
            throw new ReglaDeNegocioException("Disponibles + reservadas + vendidas debe ser igual a los cupos ("
                    + disponibles + " + " + reservadas + " + " + vendidas + " != " + cupos + ")");
        }
        if (precioBase < 0) {
            throw new ReglaDeNegocioException("El precio base no puede ser negativo (recibido: " + precioBase + ")");
        }
        if (tipoPrecio == null || parametrosPrecio == null) {
            throw new ReglaDeNegocioException("El inventario exige tipo y parámetros de precio");
        }
    }

    /** Sin estrategia indicada, el precio es fijo. */
    public InventarioCategoria(UUID eventoId, TipoBoleta categoria, int cupos, int disponibles,
                               int reservadas, int vendidas, long precioBase) {
        this(eventoId, categoria, cupos, disponibles, reservadas, vendidas, precioBase,
                TipoPrecio.FIJO, ParametrosPrecio.ninguno());
    }

    /** Inventario de un evento recién publicado: todo está disponible y el precio es fijo. */
    public static InventarioCategoria nuevo(UUID eventoId, TipoBoleta categoria, int cupos, long precioBase) {
        return new InventarioCategoria(eventoId, categoria, cupos, cupos, 0, 0, precioBase);
    }

    /** Lo que la estrategia de precio necesita saber de esta categoría. */
    public ContextoPrecio contextoPrecio(Instant fechaCompra) {
        return new ContextoPrecio(precioBase, cupos, vendidas, fechaCompra, parametrosPrecio);
    }
}