# Registro de decisiones de arquitectura (ADR)

Cada decisión importante de EventHub queda documentada aquí, con su contexto, las alternativas consideradas y sus consecuencias. Formato de nombre: `NNNN-titulo.md`.

| ADR | Decisión | Requerimientos | Responsable | Estado   | Issues |
|---|---|---|---|----------|---|
| [0001](0001-comunicacion-asincrona-rabbitmq.md) | Comunicación asíncrona con RabbitMQ | RNF-02, RF-04, RF-05 | David | Aceptado | #15, #25, #26 |
| [0002](0002-base-de-datos-por-microservicio.md) | Una base de datos por microservicio | RNF-01, RNF-02 | David | Aceptado | #8, #16–#18 |
| [0003](0003-control-de-concurrencia-y-dueno-del-aforo.md) | Concurrencia en la compra y responsable del aforo | RNF-01, RF-01, RF-05, RNF-04 | Carlos | Aceptado | #19, #20, #23 |
| [0004](0004-qr-unico-y-no-falsificable.md) | QR único y no falsificable | RNF-03, RF-03, RF-04 | Carlos | Aceptado | #24, #25, #41 |
| [0005](0005-acceso-frontend.md) | Acceso del frontend a los microservicios | RF-01–RF-03, RF-06, RNF-03 | Karol | Aceptado | #35–#38 |
| [0006](0006-replica-de-compradores-en-notificaciones.md) | Notificaciones guarda su copia de compradores | RF-04, RNF-02 | David | Aceptado | #15, #18, #25, #26 |
| [0007](0007-clean-architecture-verificada-con-archunit.md) | Clean Architecture por servicio, verificada con ArchUnit | Todos los RF, RNF-02 | Carlos, David | Aceptado | #16–#18 |
| [0008](0008-spring-boot-y-jpa-con-updates-condicionales.md) | Spring Boot + Spring Data JPA con UPDATE condicionales | RNF-01, RNF-03, RNF-04 | Carlos | Aceptado | #16–#18, #23, #24 |
| [0009](0009-errores-con-problemdetail.md) | Errores con ProblemDetail (RFC 9457) | RF-01–RF-03 | Carlos | Aceptado | #19–#23 |
| [0010](0010-estrategia-de-pruebas.md) | Estrategia de pruebas: fakes, H2, ArchUnit, concurrencia | RNF-01–RNF-03 | David | Aceptado | #19–#24, #27 |
| [0011](0011-github-flow.md) | GitHub Flow (no Gitflow) | Trabajo colaborativo | Carlos | Aceptado | #7 |
| [0012](0012-autenticacion-simulada.md) | Sesión simulada por rol, sin JWT | RF-01–RF-03, RF-06 | Karol | Aceptado | #32, #34–#38 |
| [0013](0013-ddl-auto-update-en-lugar-de-flyway.md) | `ddl-auto: update` en lugar de Flyway (deuda técnica) | Operación | David | Aceptado | #19, #21, #25 |
| [0014](0014-documentacion-de-api-code-first.md) | Documentación de API code-first (springdoc) | RF-01–RF-03, RF-06 | Carlos | Aceptado | #66, #75 |

## Fechas

La columna **Fecha** de cada ADR es el día en que la decisión entró al proyecto (según el historial de `main`). Los ADR 0007 a 0014 se redactaron el 2026-11-19, a partir de decisiones ya tomadas en el código; por eso además llevan la línea **Documentado**.

## Estados

- **Propuesto:** redactado, pendiente de revisión del equipo.
- **Aceptado:** aprobado en un PR por al menos otro integrante.
- **Reemplazado:** una decisión posterior lo sustituye (se indica cuál).
