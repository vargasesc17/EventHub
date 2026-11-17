package com.eventhub.boleteria.interfaces;

import com.eventhub.boleteria.application.ConsultarAforoUseCase.AforoConsultado;
import com.eventhub.boleteria.domain.TipoBoleta;
import com.eventhub.boleteria.domain.TipoPrecio;

import java.util.List;
import java.util.UUID;

public record AforoResponse(UUID eventoId, String nombre, int aforoMaximo, int vendidas,
                            double porcentajeVendido, boolean alertaEnviada, boolean aceptaVentas,
                            List<Categoria> categorias) {

    public record Categoria(TipoBoleta categoria, int cupos, int disponibles, int vendidas,
                            long precioBase, TipoPrecio tipoPrecio) { }

    static AforoResponse desde(AforoConsultado consulta) {
        var e = consulta.evento();
        var categorias = consulta.categorias().stream()
                .map(i -> new Categoria(i.categoria(), i.cupos(), i.disponibles(), i.vendidas(),
                        i.precioBase(), i.tipoPrecio()))
                .toList();
        double porcentaje = Math.round(e.vendidas() * 1000.0 / e.aforoMaximo()) / 10.0;
        return new AforoResponse(e.eventoId(), e.nombre(), e.aforoMaximo(), e.vendidas(), porcentaje,
                e.alertaEnviada(), e.aceptaVentas(), categorias);
    }
}