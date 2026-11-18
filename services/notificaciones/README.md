# Microservicio Notificaciones

**Responsable:** David Murcia · **Épica:** #5

Consume `boleta.comprada`, `evento.actualizado` y `evento.aforo.critico` y envía los correos de confirmación, recordatorio y alertas. Guarda su propia copia de compradores por evento (ver [ADR-0006](../../docs/adr/0006-replica-de-compradores-en-notificaciones.md)).

- Base de datos propia: `notificaciones_db` (ver [ADR-0002](../../docs/adr/0002-base-de-datos-por-microservicio.md))
- Contratos de los mensajes: [docs/contratos](../../docs/contratos/README.md)
- Issues: #18 (esqueleto), #25, #26 (consumidores), #27 (pruebas)

## Estructura (Clean Architecture)
```
src/main/java/com/eventhub/notificaciones/
├── domain/          Entidades y reglas de negocio. No depende de nadie (ni de Spring).
├── application/     Casos de uso y puertos (interfaces de Java). Solo depende de domain.
├── infrastructure/  Implementa los puertos: JPA, RabbitMQ, correo. Depende hacia adentro.
└── interfaces/      Entrada desde afuera: consumidores de RabbitMQ y tareas programadas.
```

La regla de dependencias se verifica automáticamente en `ArquitecturaTest` (ArchUnit): si una capa importa algo que no debe, `./mvnw test` falla.

## RabbitMQ

Al arrancar, el servicio declara sus colas tal como dice el contrato (#15), en `infrastructure/messaging/TopologiaRabbit.java`:

| Cola | Routing key | Cola de mensajes muertos |
|---|---|---|
| `notificaciones.boleta-comprada` | `boleta.comprada` | `notificaciones.boleta-comprada.dlq` |
| `notificaciones.evento-actualizado` | `evento.actualizado` | `notificaciones.evento-actualizado.dlq` |
| `notificaciones.aforo-critico` | `evento.aforo.critico` | `notificaciones.aforo-critico.dlq` |

Además envía un **mensaje de prueba** a la cola temporal `notificaciones.ping` y lo vuelve a leer (`MensajeDePrueba.java`). En el log debe aparecer:

```
Mensaje de prueba enviado a RabbitMQ: Hola desde Notificaciones, ...
Mensaje de prueba recibido de RabbitMQ: Hola desde Notificaciones, ... -> conexión OK
```

## Qué correos envía (RF-04, RF-05)

| Cuándo | A quién | Clase |
|---|---|---|
| Llega `boleta.comprada` | Al comprador: confirmación con el QR (#25) | `ConsumidorBoletaComprada` → `ProcesarBoletaCompradaUseCase` |
| Llega `evento.actualizado` con `FECHA`, `LUGAR` o `CANCELADO` | A cada comprador, **una sola vez** aunque tenga varias boletas (#26) | `ConsumidorEventoActualizado` → `ProcesarEventoActualizadoUseCase` |
| Llega `evento.aforo.critico` | Al organizador, una vez por evento (#26) | `ConsumidorAforoCritico` → `ProcesarAforoCriticoUseCase` |
| Faltan menos de 24 horas para el evento | A cada comprador; si cambia la fecha, se envía otro para la fecha nueva (#26) | `RecordatoriosJob` (cada minuto) → `EnviarRecordatoriosUseCase` |

`evento.actualizado` con solo `PUBLICADO` o solo `INVENTARIO` no envía correos. Los mensajes repetidos (mismo `messageId`) y los que llegan fuera de orden se ignoran.

En local los correos **no salen a internet**: los atrapa **Mailpit**. Se ven en http://localhost:8025.

## Cómo correrlo

### Con Docker (recomendado)

Desde la **raíz del repo**:

```bash
cp .env.example .env              # solo la primera vez
docker compose up -d --build
docker compose ps                 # eventhub-notificaciones debe decir (healthy)
docker logs eventhub-notificaciones   # buscar "conexión OK"
curl localhost:8083/health        # debe mostrar db y rabbit en UP
```

Las colas se ven en el panel de RabbitMQ: http://localhost:15672 (usuario y contraseña del `.env`).

### Local, desde el IDE o la terminal

Postgres y RabbitMQ siguen en Docker y la app corre en tu máquina:

```bash
# desde la raíz del repo
docker compose up -d postgres rabbitmq

# desde services/notificaciones
set -a; source ../../.env; set +a   # carga las variables del .env en esta terminal
./mvnw spring-boot:run
curl localhost:8080/health
```

> En Windows (PowerShell) el `source` no existe: define `NOTIFICACIONES_DB_USER`, `NOTIFICACIONES_DB_PASSWORD`, `RABBITMQ_USER` y `RABBITMQ_PASSWORD` como variables de entorno o en la configuración de ejecución del IDE.

### Tests

```bash
./mvnw test
```

**No necesitan Docker:** usan el perfil `test` (`src/test/resources/application-test.yml`), con una base H2 en memoria y sin RabbitMQ (el mensaje de prueba y los listeners no arrancan).

Si no tienes Java 25 instalado, puedes correrlos dentro de Docker desde `services/notificaciones`:

```bash
docker run --rm -v "${PWD}:/app" -w /app maven:3.10.0-eclipse-temurin-25 mvn -B test
```

## Problemas comunes

| Síntoma | Causa | Solución |
|---|---|---|
| `Could not resolve placeholder 'RABBITMQ_USER'` | No cargaste el `.env` en esa terminal | `set -a; source ../../.env; set +a` |
| `No se pudo conectar a RabbitMQ` en el log | RabbitMQ no está arriba o las credenciales no coinciden | `docker compose ps` y revisar `RABBITMQ_USER`/`RABBITMQ_PASSWORD` del `.env` |
| `Connection refused` desde el contenedor | Usaste `localhost` como host | Dentro de Docker el host es el nombre del servicio: `postgres` o `rabbitmq` |
| Cambié el `.env` y no toma las credenciales nuevas | El volumen ya estaba inicializado | `docker compose down -v` (borra los datos) y volver a levantar |

| Puerto | Qué es |
|---|---|
| `8080` | La app corriendo local (`./mvnw`) y el puerto **dentro** del contenedor |
| `8083` | La app en Docker, vista desde tu máquina |
