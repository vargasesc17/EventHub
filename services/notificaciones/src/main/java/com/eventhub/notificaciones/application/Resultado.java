package com.eventhub.notificaciones.application;

/** Qué hizo un caso de uso con un mensaje. Sirve para el log y para los tests. */
public enum Resultado {
    /** Se procesó y se envió al menos un correo. */
    PROCESADO,
    /** El messageId ya se había procesado: es un duplicado y no se hizo nada. */
    DUPLICADO,
    /** Llegó un mensaje más viejo que el último aplicado (fuera de orden): se ignora. */
    DESACTUALIZADO,
    /** El mensaje es válido pero no requiere enviar nada (por ejemplo, solo PUBLICADO o INVENTARIO). */
    SIN_ACCION
}
