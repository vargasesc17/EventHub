package com.eventhub.boleteria.application;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

class MensajesEnMemoria implements MensajeProcesadoRepository {
    private final Set<UUID> procesados = new HashSet<>();

    @Override
    public boolean yaProcesado(UUID messageId) { return procesados.contains(messageId); }

    @Override
    public void registrar(UUID messageId, String tipo, Instant procesadoEn) { procesados.add(messageId); }
}