/**
 * Está situada entre el dominio y las capas externas (infrastructure e interfaces).
 * Tiene los casos de uso (qué hacer cuando llega cada mensaje) y los puertos
 * (interfaces de Java como EnviadorCorreo o CompradorRepository). Sólo depende
 * del dominio; recibe los puertos por constructor, con aplicación a DIP.
 **/

package com.eventhub.notificaciones.application;
