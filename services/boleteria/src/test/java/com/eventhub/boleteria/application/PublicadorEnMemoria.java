package com.eventhub.boleteria.application;

import com.eventhub.boleteria.domain.AforoEvento;
import com.eventhub.boleteria.domain.Boleta;
import com.eventhub.boleteria.domain.Compra;

import java.util.ArrayList;
import java.util.List;

class PublicadorEnMemoria implements PublicadorBoleteria {
    private final List<Boleta> compras = new ArrayList<>();
    private final List<AforoEvento> alertas = new ArrayList<>();

    @Override
    public void boletaComprada(Boleta boleta, Compra compra, AforoEvento evento, String codigoQr) { compras.add(boleta); }

    @Override
    public void aforoCritico(AforoEvento evento, int umbral) { alertas.add(evento); }

    List<Boleta> compras() { return compras; }

    List<AforoEvento> alertas() { return alertas; }
}