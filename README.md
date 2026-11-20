# EventHub

Sistema de gestión de eventos y boletería basado en microservicios (Eventos, Boletería y Notificaciones) que se comunican por RabbitMQ, con una aplicación web en React. Trabajo final de Diseño y Arquitectura de Software.

**Equipo:** Carlos Vargas (@vargasesc17) · David Murcia (@DavidMurcia10) · Karol Esquivel (@KarolEs04)

📄 **Documento final** (C4, requerimientos, ADRs, SOLID, patrones y atributos de calidad): [`docs/entrega-final.md`](docs/entrega-final.md)

## Contenido

1. [Requisitos](#1-requisitos)
2. [Levantar todo en 3 pasos](#2-levantar-todo-en-3-pasos)
3. [Qué queda corriendo](#3-qué-queda-corriendo)
4. [Usar la aplicación](#4-usar-la-aplicación)
5. [Probar la API: Swagger y Postman](#5-probar-la-api-swagger-y-postman)
6. [Correr las pruebas automáticas](#6-correr-las-pruebas-automáticas)
7. [Apagar y empezar de cero](#7-apagar-y-empezar-de-cero)
8. [Problemas comunes](#8-problemas-comunes)
9. [Estructura del repositorio](#9-estructura-del-repositorio)

## 1. Requisitos

| Para | Necesitas |
|---|---|
| Levantar el sistema | [Docker Desktop](https://www.docker.com/products/docker-desktop/) **abierto** (con eso basta: Java, Node, PostgreSQL y RabbitMQ corren dentro de contenedores) |
| Correr las pruebas | JDK 25 (los servicios traen `./mvnw`, no hace falta instalar Maven) |
| Probar la API (opcional) | [Postman](https://www.postman.com/downloads/) |

## 2. Levantar todo en 3 pasos

```bash
git clone https://github.com/vargasesc17/EventHub.git
cd EventHub

# 1. Variables de entorno (contraseñas y secreto del QR). Nunca se sube al repo.
cp .env.example .env              # Windows (PowerShell): copy .env.example .env

# 2. Construir y levantar los 7 contenedores (la primera vez tarda unos minutos)
docker compose up -d --build

# 3. Esperar a que todo esté "healthy"
docker compose ps
```

En `docker compose ps`, `rabbitmq`, `postgres`, `eventos`, `boleteria` y `notificaciones` deben decir **(healthy)**; `mailpit` y `frontend`, **Up**. Si alguno dice `starting`, espera unos segundos y repite el comando. Los microservicios esperan a que RabbitMQ y PostgreSQL estén listos antes de arrancar.

## 3. Qué queda corriendo

| Qué | Dirección | Para qué |
|---|---|---|
| **Aplicación web** | http://localhost:8080 | La app para comprador, organizador y staff |
| Swagger de Eventos | http://localhost:8081/swagger-ui.html | Documentación interactiva de la API |
| Swagger de Boletería | http://localhost:8082/swagger-ui.html | Documentación interactiva de la API |
| **Mailpit** | http://localhost:8025 | Bandeja donde llegan **todos** los correos (no se envían de verdad) |
| Panel de RabbitMQ | http://localhost:15672 | Colas y mensajes. Usuario y contraseña: `RABBITMQ_USER` / `RABBITMQ_PASSWORD` del `.env` |
| PostgreSQL | `localhost:5432` | Una base por servicio: `eventos_db`, `boleteria_db`, `notificaciones_db` (usuarios en el `.env`) |
| Salud de cada servicio | http://localhost:8081/health · :8082 · :8083 | Debe responder `{"status":"UP"}` |

```
Navegador ──► Nginx :8080 ──┬─► /api/eventos    ──► Eventos :8081 ──┐
                            └─► /api/boleteria/ ──► Boletería :8082 ─┼─► RabbitMQ ──► Notificaciones :8083 ──► Mailpit
                                                                    └─ cada uno con su base en PostgreSQL
```

## 4. Usar la aplicación

Entra a http://localhost:8080. La autenticación es **simulada**: eliges con qué usuario entrar.

| Usuario | Rol | Qué puede hacer |
|---|---|---|
| Carlos Vargas | Organizador | Crear, editar, publicar, reprogramar y cancelar eventos; ver el aforo |
| Karol Esquivel | Comprador | Ver el catálogo, comprar boletas y ver "Mis boletas" con su QR |
| David Murcia | Staff | Hacer el check-in de un evento pegando el código del QR |

**Recorrido corto:**
1. **Organizador:** crea un evento (fecha futura) con categorías GENERAL, VIP, ESTUDIANTE y CORTESIA (esta última con precio 0) y **publícalo**.
2. **Comprador:** en el catálogo, abre el evento, compra una boleta y mira el correo con el QR en **Mailpit**. En "Mis boletas", copia el código.
3. **Staff:** elige el evento, pega el código: la primera vez **entra**, la segunda dice **ya usada**.
4. **Organizador:** cancela el evento. En Mailpit llega el aviso a cada comprador y en "Mis boletas" la boleta aparece **cancelada**.

## 5. Probar la API: Swagger y Postman

- **Swagger:** los enlaces de la tabla de arriba. El contrato también está versionado en [`docs/api/`](docs/api/) (`*.openapi.yaml`), junto con una guía de los endpoints para el frontend ([`docs/api/README.md`](docs/api/README.md)).
- **Postman:** importa [`docs/postman/EventHub.postman_collection.json`](docs/postman/EventHub.postman_collection.json) (*Import* → archivo). Es el guion de la demo: 26 peticiones en orden, que se pasan los datos entre sí con variables (`eventoId`, `codigoQr`...) y traen pruebas automáticas.

  Para correrla completa: clic derecho en la colección → **Run collection** → *Run*. Todas deben salir en verde. Cubre:

  | Carpeta | Qué demuestra |
  |---|---|
  | 0. Salud | Los tres servicios responden |
  | 1. Organizador | Crear y publicar; reglas de dominio (fecha pasada y cortesía con precio dan 422); "Mis eventos" |
  | 2. Comprador | Factory Method (cada tipo de boleta con sus reglas), Strategy (precio), agotado (409), "Mis boletas", aforo |
  | 3. Staff | QR de otro evento, check-in válido, QR repetido y QR alterado (RNF-03) |
  | 4. Cambios del evento | Observer por RabbitMQ: reprogramar y cancelar avisan a los compradores y cancelan sus boletas |

  > La colección crea un evento nuevo en cada ejecución, así que se puede correr varias veces seguidas.

## 6. Correr las pruebas automáticas

Cada servicio tiene pruebas unitarias, de integración (con H2 en memoria, sin Docker) y de arquitectura (ArchUnit verifica las capas de Clean Architecture):

```bash
cd services/eventos        && ./mvnw clean test && cd ../..
cd services/boleteria      && ./mvnw clean test && cd ../..
cd services/notificaciones && ./mvnw clean test && cd ../..
```

Debe terminar con `BUILD SUCCESS` en los tres. Las pruebas destacadas son `CompraConcurrenteTest` (100 compradores por 10 cupos: nunca se vende de más), `CheckInDePuntaAPuntaTest` y `ArquitecturaTest`. Frontend: `cd frontend && npm ci && npm run lint && npm run build`.

## 7. Apagar y empezar de cero

```bash
docker compose down        # apaga; los datos se conservan
docker compose down -v     # apaga y BORRA los datos (eventos, boletas, colas)
```

Después de un `down -v`, abre la consola del navegador en http://localhost:8080 y ejecuta `localStorage.clear()` para cerrar la sesión simulada guardada.

Para reconstruir después de cambiar código: `docker compose up -d --build`.

## 8. Problemas comunes

| Síntoma | Causa y solución |
|---|---|
| `Cannot connect to the Docker daemon` | Docker Desktop no está abierto. Ábrelo, espera a que diga *running* y repite. |
| `The "RABBITMQ_USER" variable is not set` (u otras variables) | Falta el paso 1: `cp .env.example .env`. |
| Un servicio no llega a *healthy* y su log dice `password authentication failed` | La base se creó con otro `.env`. Ejecuta `docker compose down -v` y vuelve a levantar. |
| `network ... not found` al levantar | Quedó un contenedor de una ejecución anterior: `docker rm -f eventhub-frontend` (o el que diga el error) y repite. |
| `port is already allocated` | Otro programa usa ese puerto (8080, 5432, 5672...). Ciérralo o apaga el otro proyecto de Docker. |
| Ver qué pasó en un servicio | `docker compose logs -f boleteria` (o `eventos`, `notificaciones`, `frontend`). |
| No llegan correos | Revisa http://localhost:8025 y `docker compose logs notificaciones`. Los mensajes esperan en RabbitMQ si Notificaciones estaba caído. |
| `./mvnw: Permission denied` | `chmod +x mvnw` dentro del servicio. |

## 9. Estructura del repositorio

```
/docs                    Documento final, requerimientos, diagramas C4 y de clases, ADRs,
                         contratos de mensajes, API (OpenAPI) y colección de Postman
/services/eventos        Microservicio Eventos         (Spring Boot, Clean Architecture)
/services/boleteria      Microservicio Boletería       (Spring Boot, Clean Architecture)
/services/notificaciones Microservicio Notificaciones  (Spring Boot, Clean Architecture)
/frontend                Aplicación web (React + Vite, servida por Nginx)
/infra                   Imagen de PostgreSQL con el script que crea una base y un usuario por servicio
docker-compose.yml       Todo el sistema
```

## Cómo trabajamos

Todo cambio entra a `main` por pull request con al menos una aprobación. Ramas, commits y revisión: ver [CONTRIBUTING.md](CONTRIBUTING.md).
