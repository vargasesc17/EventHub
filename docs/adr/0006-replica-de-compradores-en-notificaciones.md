# ADR-0006: Notificaciones mantiene su propia copia de los compradores por evento

**Estado:** Aceptado
**Fecha:** 2026-11-09
**Decisores:** Carlos Vargas, David Murcia, Karol Esquivel
**Responsable:** David Murcia (dueño del servicio Notificaciones)
**Requerimientos que atiende:** RF-04 (Alertas de cambio o cancelación), RNF-02 (Disponibilidad)
**Issues relacionados:** #15, #18, #25, #26

## Contexto

Cuando un evento cambia de fecha o lugar, o se cancela, Eventos publica `evento.actualizado` y Notificaciones debe avisar **a todos los compradores de ese evento** (RF-04).

El problema es que la lista de compradores la tiene **Boletería**, no Notificaciones. El primer borrador del diagrama de Contenedores resolvía esto con una llamada HTTP de Notificaciones a Boletería. Eso crea una dependencia: **si Boletería está caída cuando llega el mensaje, la alerta de cancelación no se puede enviar**.

## Decisión

Notificaciones guarda **su propia copia** de los datos que necesita, en `notificaciones_db`:

- Al consumir `boleta.comprada`, registra el `eventoId`, el `boletaId` y el correo del comprador.
- Al consumir `evento.actualizado`, busca los destinatarios en su propia base. **No llama a ningún otro servicio.**

Para que funcione, el mensaje `boleta.comprada` debe incluir el **correo del comprador** (se define en el contrato, #15). Si se cancela una boleta, Boletería deberá publicar un evento para que Notificaciones la retire de su copia.

## Opciones consideradas

### Opción A: Notificaciones consulta los compradores a Boletería por HTTP

| Dimensión | Evaluación |
|---|---|
| Complejidad | Baja |
| Disponibilidad | Baja: depende de que Boletería esté arriba |
| Consistencia | Fuerte: siempre lee el dato actual |

**Ventajas:** no duplica datos; la lista siempre está al día.
**Desventajas:** acopla la disponibilidad de los dos servicios; para un evento grande, la consulta puede devolver miles de registros de una vez.

### Opción B: el mensaje `evento.actualizado` incluye la lista de destinatarios

**Desventajas:** Eventos tampoco conoce a los compradores; tendría que pedírselos a Boletería, lo que solo traslada el problema. Descartada.

### Opción C: réplica local alimentada por eventos ✅

| Dimensión | Evaluación |
|---|---|
| Complejidad | Media |
| Disponibilidad | Alta: Notificaciones funciona solo |
| Consistencia | Eventual: la copia puede tardar unos segundos |

**Ventajas:** Notificaciones es autónomo; no hay llamadas síncronas entre servicios.
**Desventajas:** datos duplicados; si un mensaje se pierde, la copia queda incompleta.

## Análisis del trade-off

Es el trade-off clásico entre **consistencia y disponibilidad**. El enunciado prioriza la disponibilidad, y para este caso la consistencia eventual es inofensiva: solo un comprador que compró en los últimos segundos antes de una cancelación podría quedarse sin la alerta, y aun así recibirá la confirmación de su compra.

Esta decisión depende de que RabbitMQ no pierda mensajes (ADR-0001): colas durables, mensajes persistentes y `ack` manual son los que garantizan que la copia quede completa.

## Consecuencias

- **Más fácil:** Notificaciones se puede probar y desplegar de forma independiente.
- **Más difícil:** mantener la copia sincronizada; cualquier cambio en los compradores (cancelación de boleta, cambio de correo) necesita un evento.
- **Datos personales:** el correo del comprador queda guardado en dos servicios. Hay que tenerlo en cuenta en cualquier política de protección de datos.
- **Diagrama:** el C2 ya refleja esta decisión (BD Notificaciones y sin flecha hacia Boletería).

## Acciones

1. [ ] Incluir el correo del comprador en el contrato de `boleta.comprada` (#15).
2. [ ] Crear la tabla de compradores por evento en `notificaciones_db` (#18).
3. [ ] Guardar el comprador al consumir `boleta.comprada` (#25).
4. [ ] Usar la copia local al consumir `evento.actualizado` (#26).
5. [x] **Agregar este ADR a la lista del issue #13.**
6. [ ] **Pendiente (fuera de esta entrega):** publicar `boleta.cancelada` desde Boletería para que Notificaciones retire la boleta de su copia. Dentro del alcance actual ningún flujo cancela una boleta ya pagada; se decidió en la revisión de los contratos (#15).
