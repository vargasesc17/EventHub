package com.eventhub.notificaciones.application;

import java.util.UUID;

/** Lo que el caso de uso necesita de un evento.aforo.critico (docs/contratos). */
public record AforoCriticoComando(UUID messageId, UUID eventoId, String nombreEvento, String correoOrganizador,
                                  int aforoMaximo, int vendidas, double porcentajeVendido, int umbral) {
}
