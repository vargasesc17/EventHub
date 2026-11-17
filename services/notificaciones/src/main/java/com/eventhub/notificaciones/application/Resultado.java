package com.eventhub.notificaciones.application;

/** Qué hizo un caso de uso con un mensaje. Sirve para el log y para los tests. */
public enum Resultado {
    /** Se procesó y se envió el correo. */
    PROCESADO,
    /** El messageId ya se había procesado: es un duplicado y no se hizo nada. */
    DUPLICADO
}
