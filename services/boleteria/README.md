# Microservicio Boletería

**Responsable:** Carlos Vargas · **Épica:** #4

Compra y reserva de boletas, inventario por categoría (dueño del aforo, ver [ADR-0003](../../docs/adr/0003-control-de-concurrencia-y-dueno-del-aforo.md)), generación del QR firmado y check-in. Publica `boleta.comprada` y `evento.aforo.critico`; consume `evento.actualizado`.

- Base de datos propia: `boleteria_db`
- Issues: #17 (esqueleto), #21 (Factory Method), #22 (Strategy), #23 (compra), #24 (QR y check-in)
