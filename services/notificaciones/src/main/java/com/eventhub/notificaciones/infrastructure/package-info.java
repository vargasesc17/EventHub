/**
 * Es la capa más externa del modelo. Tiene lo técnico del sistema: la
 * configuración de RabbitMQ, la base de datos (JPA) y el envío de correos
 * (SMTP). Implementa los puertos de la capa de application. De ella no depende
 * ninguna capa, pero sí puede depender de las internas.
 **/

package com.eventhub.notificaciones.infrastructure;
