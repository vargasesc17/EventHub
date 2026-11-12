# Microservicio Boletería

**Responsable:** Carlos Vargas · **Épica:** #4

Creación y configuración de eventos, categorías de boleta y ciclo de vida del evento (borrador → publicado → en curso → finalizado / cancelado). Publica `evento.actualizado` en RabbitMQ.

- Base de datos propia: `boleteria_db` (ver [ADR-0002](../../docs/adr/0002-base-de-datos-por-microservicio.md))
- Issues: #17 (esqueleto), #21 (Factory Method), #22 (Strategy), #23 (compra), #24 (QR y check-in)

## Estructura (Clean Architecture)
```
src/main/java/com/eventhub/boleteria/
├── domain/ Entidades y reglas de negocio. No depende de nadie (ni de Spring).
├── application/ Casos de uso y puertos (interfaces de Java). Solo depende de domain.
├── infrastructure/ Implementa los puertos: JPA, RabbitMQ. Depende hacia adentro.
└── interfaces/ Entrada desde afuera: controladores REST y DTOs. Depende hacia adentro.
```

La regla de dependencias se verifica automáticamente en `ArquitecturaTest` (ArchUnit): si una capa importa algo que no debe, `./mvnw test` falla.

## Cómo correrlo

### Con Docker (recomendado)

Desde la **raíz del repo**:

```bash
cp .env.example .env              # solo la primera vez
docker compose up -d --build
docker compose ps                 # eventhub-boleteria debe decir (healthy)
curl localhost:8082/health
```

### Local, desde el IDE o la terminal

Útil mientras desarrollas. Postgres sigue corriendo en Docker y la app corre en tu máquina:

```bash
# desde la raíz del repo
docker compose up -d postgres

# desde services/boleteria
set -a; source ../../.env; set +a   # carga las variables del .env en esta terminal
./mvnw spring-boot:run
curl localhost:8082/health
```

> En Windows (PowerShell) el `source` no existe: define `BOLETERIA_DB_USER` y `BOLETERIA_DB_PASSWORD` como variables de entorno o en la configuración de ejecución del IDE.

### Tests

```bash
./mvnw test
```

**No necesitan Docker:** usan el perfil `test` (`src/test/resources/application-test.yml`), con una base H2 en memoria.

## Problemas comunes

| Síntoma | Causa | Solución |
|---|---|---|
| `Could not resolve placeholder 'BOLETERIA_DB_USER'` | No cargaste el `.env` en esa terminal | `set -a; source ../../.env; set +a` |
| Postgres sale `Exited (1)` con *"superuser password is not specified"* | No existe `.env` en la raíz | `cp .env.example .env` y `docker compose down -v && docker compose up -d` |
| `Connection refused` a Postgres desde el contenedor | Usaste `localhost` como host de la base | Dentro de Docker el host es el nombre del servicio: `postgres` |
| Cambié el `.env` y no toma las credenciales nuevas | El volumen ya estaba inicializado | `docker compose down -v` (borra los datos) y volver a levantar |

| Puerto | Qué es |
|---|---|
| `8080` | La app corriendo local (`./mvnw`) y el puerto **dentro** del contenedor |
| `8082` | La app en Docker, vista desde tu máquina |