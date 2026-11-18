package com.eventhub.boleteria.application;

import com.eventhub.boleteria.domain.AforoEvento;
import com.eventhub.boleteria.domain.Boleta;
import com.eventhub.boleteria.domain.Compra;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Publicador para los tests: anota los mensajes en vez de enviarlos. Seguro con varios hilos. */
@Component
public class PublicadorEnMemoria implements PublicadorBoleteria {

    private final List<Boleta> compras = new CopyOnWriteArrayList<>();
    private final List<AforoEvento> alertas = new CopyOnWriteArrayList<>();

    @Override
    public void boletaComprada(Boleta boleta, Compra compra, AforoEvento evento, String codigoQr) { compras.add(boleta); }

    @Override
    public void aforoCritico(AforoEvento evento, int umbral) { alertas.add(evento); }

    public List<Boleta> compras() { return compras; }

    public List<AforoEvento> alertas() { return alertas; }

    public void limpiar() {
        compras.clear();
        alertas.clear();
    }
}