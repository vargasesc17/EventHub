/**
 * Es la capa por donde entran recursos del exterior: los consumidores de
 * RabbitMQ (boleta.comprada, evento.actualizado, evento.aforo.critico) y las
 * tareas programadas. Depende de las capas internas. Llama a los casos de uso.
 **/

package com.eventhub.notificaciones.interfaces;
