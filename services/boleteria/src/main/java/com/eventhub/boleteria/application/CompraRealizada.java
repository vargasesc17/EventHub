package com.eventhub.boleteria.application;

import com.eventhub.boleteria.domain.Boleta;

public record CompraRealizada(Boleta boleta, String codigoQr) { }