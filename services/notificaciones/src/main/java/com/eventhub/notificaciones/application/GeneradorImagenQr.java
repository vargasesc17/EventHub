package com.eventhub.notificaciones.application;

/**
 * Convierte el texto del QR en una imagen PNG. Notificaciones solo dibuja el QR que generó
 * Boletería; no lo firma ni lo valida (ADR-0004).
 */
public interface GeneradorImagenQr {

    byte[] generarPng(String contenido);
}
