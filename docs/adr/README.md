# Registro de decisiones de arquitectura (ADR)

Cada decisión importante de EventHub queda documentada aquí, con su contexto, las alternativas consideradas y sus consecuencias. Formato de nombre: `NNNN-titulo.md`.

| ADR | Decisión | Requerimientos | Responsable | Estado | Issues |
|---|---|---|---|---|---|
| [0001](0001-comunicacion-asincrona-rabbitmq.md) | Comunicación asíncrona con RabbitMQ | RNF-02, RF-04, RF-05 | David | Propuesto | #15, #25, #26 |
| [0002](0002-base-de-datos-por-microservicio.md) | Una base de datos por microservicio | RNF-01, RNF-02 | David | Propuesto | #8, #16–#18 |
| [0003](0003-control-de-concurrencia-y-dueno-del-aforo.md) | Concurrencia en la compra y responsable del aforo | RNF-01, RF-01, RF-05, RNF-04 | Carlos | Propuesto | #19, #20, #23 |
| [0004](0004-qr-unico-y-no-falsificable.md) | QR único y no falsificable | RNF-03, RF-03, RF-04 | Carlos | Propuesto | #24, #25, #41 |
| [0005](0005-acceso-frontend.md) | Acceso del frontend a los microservicios | RF-01–RF-03, RF-06, RNF-03 | Karol | Propuesto (pendiente) | #35–#38 |
| [0006](0006-replica-de-compradores-en-notificaciones.md) | Notificaciones guarda su copia de compradores | RF-04, RNF-02 | David | Propuesto | #15, #18, #25, #26 |

## Estados

- **Propuesto:** redactado, pendiente de revisión del equipo.
- **Aceptado:** aprobado en un PR por al menos otro integrante.
- **Reemplazado:** una decisión posterior lo sustituye (se indica cuál).
