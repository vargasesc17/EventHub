# Requerimientos de EventHub

**Estado:** Propuesto, pendiente de revisión de Carlos, David y Karol (#10)

Este documento define los 10 requerimientos del sistema: 6 funcionales (RF) y 4 no funcionales (RNF). Los ADRs (`docs/adr/`) y el diagrama de clases se justifican a partir de ellos. Los nombres de contenedores corresponden al diagrama C4 de Contenedores (`docs/diagramas/c2-contenedores.svg`).

**Prioridad (MoSCoW):** **M**ust (obligatorio para la entrega), **S**hould (importante, no bloquea), **C**ould (deseable), **W**on't (fuera de alcance en esta entrega).

## Resumen y trazabilidad

| ID | Requerimiento | Prioridad | Servicio responsable | Contenedores C4 que lo atienden | ADRs | Issues |
|---|---|---|---|---|---|---|
| RF-01 | Compra y reserva de boletas por categoría | M | Boletería | Aplicación Web, Microservicio Boletería, BD Boletería, Pasarela de Pagos, Broker de Mensajes | 0003 | #21, #22, #23, #40 |
| RF-02 | Creación de eventos y gestión de su ciclo de vida | M | Eventos | Aplicación Web, Microservicio Eventos, BD Eventos, Broker de Mensajes | — | #19, #20, #42 |
| RF-03 | Check-in con QR de uso único | M | Boletería | Aplicación Web, Microservicio Boletería, BD Boletería | 0004 | #24, #41 |
| RF-04 | Notificaciones por correo a los compradores | M | Notificaciones | Broker de Mensajes, Microservicio Notificaciones, BD Notificaciones, Proveedor de Correo | 0001, 0006 | #25, #26 |
| RF-05 | Consulta del aforo y alerta al 90 % | M | Boletería (cálculo y publicación), Notificaciones (envío) | Aplicación Web, Microservicio Boletería, Broker de Mensajes, Microservicio Notificaciones, Proveedor de Correo | 0001, 0003 | #23, #26 |
| RF-06 | Catálogo de eventos disponibles | M | Eventos | Aplicación Web, Microservicio Eventos, BD Eventos | 0005 | #19, #39 |
| RNF-01 | Consistencia: no sobrevender el aforo | M | Boletería | Microservicio Boletería, BD Boletería | 0002, 0003 | #23, #27 |
| RNF-02 | Disponibilidad ante caída de Notificaciones | M | Todos | Broker de Mensajes, Microservicio Boletería, Microservicio Eventos, Microservicio Notificaciones | 0001, 0002, 0006 | #15, #28 |
| RNF-03 | Seguridad: QR único y no falsificable | M | Boletería | Microservicio Boletería, BD Boletería | 0004 | #24, #27 |
| RNF-04 | Rendimiento del catálogo y la compra | M | Eventos, Boletería | Aplicación Web, Microservicio Eventos, Microservicio Boletería | 0003, 0005 | #28 |

## Requerimientos funcionales

### RF-01: Compra y reserva de boletas

El comprador debe poder comprar y reservar boletas para un evento específico. Las boletas se gestionan por categorías: General, VIP y Estudiante. Una reserva aparta las boletas durante 24 horas; si no se paga en ese plazo, las boletas vuelven al inventario.

**Criterios de aceptación**
- Una compra pagada crea una boleta en estado `activa` con su categoría y comprador, descuenta 1 del inventario de esa categoría y publica `boleta.comprada`.
- Una reserva descuenta el inventario de inmediato. Si pasan 24 horas sin pago, la reserva se cancela y la cantidad disponible vuelve al valor anterior.
- Una compra o reserva en una categoría sin cupo se rechaza con el mensaje "sin cupo" y no modifica el inventario.

### RF-02: Creación de eventos y ciclo de vida

El organizador debe poder crear un evento con nombre, lugar, fecha, aforo máximo y categorías con precios diferenciados. También debe poder gestionar el ciclo de vida de sus eventos: borrador, publicado, en curso, finalizado y cancelado.

**Criterios de aceptación**
- Un evento nuevo queda en `borrador` y no aparece en el catálogo (RF-06) hasta que se publica.
- Solo se permiten estas transiciones: borrador → publicado, publicado → en curso, en curso → finalizado, y borrador / publicado → cancelado. Cualquier otra se rechaza.
- Un cambio de fecha o lugar, o una cancelación, de un evento publicado publica `evento.actualizado`.

### RF-03: Check-in con QR de uso único

El staff de check-in debe poder validar el QR de una boleta el día del evento. Una vez leído, la boleta pasa a estado `usada`, de modo que una segunda lectura del mismo QR no sea válida.

**Criterios de aceptación**
- Al escanear un QR válido de una boleta `activa`, el sistema responde "acceso permitido", cambia la boleta a `usada` y registra el check-in con fecha, hora y quién lo validó.
- Una segunda lectura del mismo QR responde "boleta ya usada" y no crea otro check-in.
- Un QR de una boleta `cancelada` o de otro evento se rechaza.

### RF-04: Notificaciones por correo a los compradores

Los compradores deben recibir por correo la confirmación de compra con el QR, un recordatorio 24 horas antes del evento y una alerta si el evento cambia de fecha, hora o lugar, o se cancela.

**Criterios de aceptación**
- Tras una compra, el comprador recibe un correo con el QR de su boleta.
- 24 horas antes del inicio del evento, cada comprador recibe un recordatorio.
- Cuando cambia la fecha, la hora o el lugar de un evento, o se cancela, todos sus compradores reciben la alerta, cada uno una sola vez.

### RF-05: Consulta del aforo y alerta al 90 %

El organizador debe poder ver el aforo de su evento (boletas vendidas frente al aforo máximo) y recibir una alerta cuando las ventas superen el 90 % del aforo.

**Criterios de aceptación**
- El organizador ve, por evento y por categoría, las boletas vendidas, las reservadas y las disponibles.
- Cuando una venta hace que las boletas vendidas superen el 90 % del aforo, Boletería publica `evento.aforo.critico` y el organizador recibe un correo.
- La alerta se envía una sola vez por evento, no en cada venta posterior al 90 %.

### RF-06: Catálogo de eventos disponibles

El comprador debe poder consultar el catálogo de los eventos disponibles en el momento.

**Criterios de aceptación**
- El catálogo muestra solo los eventos en estado `publicado` cuya fecha no ha pasado, con nombre, lugar, fecha y categorías con su precio.
- Los eventos en `borrador`, `finalizado` o `cancelado` no aparecen.

## Requerimientos no funcionales

### RNF-01: Consistencia

No se pueden vender más boletas que el aforo disponible. La compra debe tener control de concurrencia: si varias personas compran al mismo tiempo la última boleta disponible, solo una compra se confirma.

**Criterio medible:** con 100 solicitudes de compra simultáneas sobre una categoría con 1 boleta disponible, se confirma exactamente 1 compra, las otras 99 reciben "sin cupo" y el inventario final es 0, nunca negativo. Se verifica con una prueba automatizada de concurrencia.

### RNF-02: Disponibilidad

Se deben poder consultar los eventos disponibles y comprar boletas aunque el servicio de Notificaciones esté caído. Al recuperarse, Notificaciones envía las notificaciones pendientes sin perder ninguna.

**Criterio medible:** con el contenedor de Notificaciones detenido se hacen N compras, y el 100 % se completan con éxito. Al volver a encender Notificaciones, se envían las N confirmaciones pendientes: 0 mensajes perdidos y 0 duplicados.

### RNF-03: Seguridad

En el check-in se valida la integridad de los QR, que deben ser únicos y no falsificables.

**Criterio medible:** el 100 % de los QR alterados (por ejemplo, con un carácter cambiado) o generados sin la clave del sistema se rechazan en el check-in. Dos boletas nunca comparten el mismo código, y conocer el código de una boleta no permite deducir el de otra.

### RNF-04: Rendimiento

El 95 % de las consultas al catálogo responden en menos de 3 segundos. El sistema soporta 100 compras concurrentes sin degradar estos tiempos. El tiempo de la pasarela de pagos no se cuenta.

**Criterio medible:** en una prueba de carga con 100 compras concurrentes en curso, el percentil 95 del tiempo de respuesta del catálogo es menor a 3 s, y el percentil 95 de la compra, sin contar la pasarela de pagos, es menor a 3 s.

## Fuera de alcance (Could)

- Autenticación JWT con roles (#32).
- Panel del organizador con estadísticas en tiempo real y PDF de la boleta con QR (#33).
