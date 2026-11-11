# EventHub

Sistema de gestión de eventos y boletería basado en microservicios (Eventos, Boletería y Notificaciones) que se comunican por RabbitMQ. Trabajo final de Diseño y Arquitectura de Software.

**Equipo:** Carlos Vargas (@vargasesc17) · David Murcia (@DavidMurcia10) · Karol Esquivel (@KarolEs04)

## Estructura del repositorio

```
/docs                    Requerimientos, diagramas C4 y ADRs
/services/eventos        Microservicio Eventos
/services/boleteria      Microservicio Boletería
/services/notificaciones Microservicio Notificaciones
```

## Cómo trabajamos

Todo cambio entra a `main` por pull request con al menos una aprobación. Ramas, commits y revisión: ver [CONTRIBUTING.md](CONTRIBUTING.md).

## Levantar la infraestructura local

Necesitas [Docker Desktop](https://www.docker.com/products/docker-desktop/) abierto.

```bash
cp .env.example .env        # en Windows (PowerShell): copy .env.example .env
docker compose up -d
docker compose ps           # rabbitmq y postgres deben decir "healthy"
```

| Servicio | Dirección | Usuario / contraseña |
|---|---|---|
| Panel de RabbitMQ | http://localhost:15672 | `RABBITMQ_USER` / `RABBITMQ_PASSWORD` del `.env` |
| RabbitMQ (AMQP, para los servicios) | `localhost:5672` | igual que arriba |
| PostgreSQL | `localhost:5432` | Un usuario por base: `eventos_db`, `boleteria_db`, `notificaciones_db` (ver `.env`) |

Para apagar: `docker compose down`. Para borrar también los datos y empezar de cero: `docker compose down -v`.

> La guía completa de ejecución (microservicios, frontend y demo) se agrega en #30.
