package com.eventhub.boleteria.application;

import java.util.UUID;

/**
 * Lo que necesita el staff en la entrada: validar un QR. Está separada de la compra a propósito
 * (ISP): quien hace check-in no ve los métodos de comprar, y quien compra no ve los de check-in.
 */
public interface CheckInService {
    /**
     * @param eventoId el evento en cuya entrada está el staff: una boleta de otro evento no entra,
     *                 aunque su QR sea auténtico y esté ACTIVA.
     */
    ResultadoCheckIn validar(String codigoQr, UUID eventoId, UUID staffId);
}
