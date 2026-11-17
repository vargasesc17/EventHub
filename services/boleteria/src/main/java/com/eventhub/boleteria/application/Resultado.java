package com.eventhub.boleteria.application;

/** Qué hizo un caso de uso con un mensaje. Sirve para el log y para los tests. */
public enum Resultado {
    /** El mensaje se aplicó. */
    PROCESADO,
    /** El messageId ya se había procesado: no se hizo nada. */
    DUPLICADO,
    /** El mensaje es válido pero no cambia nada en Boletería. */
    IGNORADO
}