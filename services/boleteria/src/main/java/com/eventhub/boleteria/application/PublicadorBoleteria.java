package com.eventhub.boleteria.application;

import com.eventhub.boleteria.domain.AforoEvento;
import com.eventhub.boleteria.domain.Boleta;
import com.eventhub.boleteria.domain.Compra;

/** Los mensajes que publica Boletería (docs/contratos). */
public interface PublicadorBoleteria {

    void boletaComprada(Boleta boleta, Compra compra, AforoEvento evento, String codigoQr);

    void aforoCritico(AforoEvento evento, int umbral);
}