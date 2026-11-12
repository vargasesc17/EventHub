# Microservicio Eventos

**Responsable:** Carlos Vargas · **Épica:** #3

Creación y configuración de eventos, categorías de boleta y ciclo de vida del evento (borrador → publicado → en curso → finalizado / cancelado). Publica `evento.actualizado` en RabbitMQ.

- Base de datos propia: `eventos_db` (ver [ADR-0002](../../docs/adr/0002-base-de-datos-por-microservicio.md))
- Issues: #16 (esqueleto), #19 (CRUD), #20 (ciclo de vida)

**Cómo correrlo:**
Local: levantar la infra, cargar el `.env` y `./mvnw spring-boot:run`, en `localhost:8080/health`.

Docker: `docker compose up -d --build`, en `localhost:8081/health`.

Test: `./mvnw test`, ¡no necesita Docker corriendo!


