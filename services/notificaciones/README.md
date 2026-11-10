# Microservicio Notificaciones

**Responsable:** David Murcia · **Épica:** #5

Consume `boleta.comprada`, `evento.actualizado` y `evento.aforo.critico` y envía los correos de confirmación, recordatorio y alertas. Guarda su propia copia de compradores por evento (ver [ADR-0006](../../docs/adr/0006-replica-de-compradores-en-notificaciones.md)).

- Base de datos propia: `notificaciones_db`
- Issues: #18 (esqueleto), #25, #26 (consumidores)
