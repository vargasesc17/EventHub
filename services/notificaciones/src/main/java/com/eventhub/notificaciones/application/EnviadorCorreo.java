package com.eventhub.notificaciones.application;

import com.eventhub.notificaciones.domain.Correo;

/**
 * Envía correos. En local la implementación usa SMTP hacia Mailpit; en producción sería un
 * proveedor real. Los casos de uso no saben cuál es (DIP).
 */
public interface EnviadorCorreo {

    void enviar(Correo correo);
}
