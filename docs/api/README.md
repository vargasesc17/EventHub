# API de EventHub para el frontend

Endpoints disponibles para las pantallas del frontend. Todo el JSON va en `camelCase`, las fechas en ISO-8601 UTC (`"2027-06-01T20:00:00Z"`) y los precios en pesos colombianos enteros, sin decimales.

> Documentación interactiva (con los servicios corriendo): Eventos en http://localhost:8081/swagger-ui.html,
> Boletería en http://localhost:8082/swagger-ui.html. El contrato OpenAPI versionado está en
> `eventos.openapi.yaml` y `boleteria.openapi.yaml` (se pueden abrir en https://editor.swagger.io).

## Cómo llamarlos

| Desde | Eventos | Boletería |
|---|---|---|
| **El frontend** (a través de Nginx) | `/api/eventos/...` | `/api/boleteria/...` |
| Postman o `curl` (directo al servicio) | `http://localhost:8081/eventos/...` | `http://localhost:8082/...` |

El frontend usa siempre **rutas relativas**, sin `localhost` ni puerto:

```js
const respuesta = await fetch("/api/eventos/catalogo");
const eventos = await respuesta.json();
```

Así no hay problemas de CORS y no hay URLs quemadas en el código (ADR-0005).

> **Pendiente:** para que `/api/boleteria/...` funcione hay que corregir `frontend/nginx.conf` (ver la última sección).

## Errores

Todos los errores tienen la misma forma (`ProblemDetail`, RFC 9457). El texto de `detail` está en español y se puede mostrar al usuario tal cual:

```json
{
  "status": 422,
  "title": "Unprocessable Entity",
  "detail": "La suma de cupos (1100) supera el aforo máximo (1000)"
}
```

Con **400** por campos inválidos, además trae qué campo falló:

```json
{
  "status": 400,
  "detail": "Hay campos inválidos",
  "errores": { "nombre": "must not be blank", "correoComprador": "must be a well-formed email address" }
}
```

| Código | Significa | Qué mostrar |
|---|---|---|
| 400 | El formulario tiene campos inválidos | Marcar los campos de `errores` |
| 402 | El pago fue rechazado | `detail` |
| 404 | El evento no existe | "Evento no encontrado" |
| 409 | No quedan boletas de esa categoría | "Agotado" |
| 422 | Se rompió una regla del negocio | `detail` |

## Valores fijos

| Campo | Valores |
|---|---|
| Estado del evento | `BORRADOR`, `PUBLICADO`, `EN_CURSO`, `FINALIZADO`, `CANCELADO` |
| Categoría de boleta | `GENERAL`, `VIP`, `ESTUDIANTE`, `CORTESIA` |
| Estado de la boleta | `ACTIVA`, `USADA`, `CANCELADA`, `RESERVADA` |
| Tipo de precio | `FIJO`, `EARLY_BIRD`, `DINAMICO` |

---

## Eventos

### `GET /api/eventos/catalogo` · Catálogo del comprador

Eventos `PUBLICADO` con fecha futura (RF-06). Funciona aunque Notificaciones esté caído.

**200**
```json
[
  {
    "id": "3c1f9d2e-8a4b-4f6c-9e1d-2b7a5c8e0f13",
    "nombre": "Concierto de fin de semestre",
    "lugar": "Auditorio principal, Chía",
    "fechaInicio": "2027-06-01T20:00:00Z",
    "aforoMaximo": 500,
    "organizadorId": "a9e2c4b1-6d3f-4a8e-b2c7-1f5d9e3a7c20",
    "organizadorCorreo": "organizador@eventhub.test",
    "estado": "PUBLICADO",
    "categorias": [
      { "tipo": "GENERAL", "cupos": 400, "precioBase": 80000 },
      { "tipo": "VIP", "cupos": 100, "precioBase": 250000 }
    ]
  }
]
```

### `GET /api/eventos/{id}` · Detalle de un evento

**200** con un evento (misma forma que cada elemento del catálogo) · **404**

### `POST /api/eventos` · Crear un evento (organizador)

Queda en `BORRADOR`. La suma de cupos de las categorías no puede superar el aforo.

```json
{
  "nombre": "Concierto de fin de semestre",
  "lugar": "Auditorio principal, Chía",
  "fechaInicio": "2027-06-01T20:00:00Z",
  "aforoMaximo": 500,
  "organizadorId": "a9e2c4b1-6d3f-4a8e-b2c7-1f5d9e3a7c20",
  "organizadorCorreo": "organizador@eventhub.test",
  "categorias": [
    { "tipo": "GENERAL", "cupos": 400, "precioBase": 80000 },
    { "tipo": "VIP", "cupos": 100, "precioBase": 250000 }
  ]
}
```

**201** con el evento creado (incluye el `id`) · **400** campos inválidos · **422** los cupos superan el aforo

### `PUT /api/eventos/{id}` · Editar un borrador

Reemplaza todos los datos. Solo funciona en `BORRADOR`. Mismo cuerpo que crear, **sin** `organizadorId` ni `organizadorCorreo`.

**200** · **400** · **404** · **422** (no está en borrador, o los cupos superan el aforo)

### `PATCH /api/eventos/{id}` · Cambiar fecha y/o lugar de un evento publicado

Avisa por correo a los compradores. Se envía solo lo que cambia:

```json
{ "fechaInicio": "2027-06-03T20:00:00Z", "lugar": "Coliseo, Chía" }
```

**200** · **404** · **422** (el evento no está publicado)

### Cambios de estado (sin cuerpo)

| Endpoint | Transición | Qué pasa |
|---|---|---|
| `POST /api/eventos/{id}/publicar` | Borrador → Publicado | Aparece en el catálogo y Boletería abre la venta |
| `POST /api/eventos/{id}/iniciar` | Publicado → En curso | |
| `POST /api/eventos/{id}/finalizar` | En curso → Finalizado | |
| `POST /api/eventos/{id}/cancelar` | Borrador o Publicado → Cancelado | Se cierra la venta y se avisa a los compradores |

Todas responden **200** con el evento actualizado, **404** si no existe y **422** si la transición no es válida (por ejemplo, finalizar un borrador). En ese caso `detail` dice de qué estado a cuál, así que conviene mostrar solo los botones que aplican al estado actual.

---

## Boletería

> Disponibles cuando se una el PR #65.

### `POST /api/boleteria/boletas` · Comprar una boleta

```json
{
  "eventoId": "3c1f9d2e-8a4b-4f6c-9e1d-2b7a5c8e0f13",
  "categoria": "VIP",
  "compradorId": "e4b7a1c2-9d3e-4f5a-8b6c-7d8e9f0a1b2c",
  "nombreComprador": "Ana Gómez",
  "correoComprador": "ana@correo.test",
  "carnetEstudiantil": null,
  "emitidaPor": null
}
```

- `carnetEstudiantil` es **obligatorio** para `ESTUDIANTE`.
- `emitidaPor` (UUID de quien regala la boleta) es **obligatorio** para `CORTESIA`.
- En las demás categorías se pueden omitir.
- Como no hay inicio de sesión, el frontend puede generar el `compradorId` con `crypto.randomUUID()` y guardarlo en el navegador.

**201**
```json
{
  "boletaId": "5d8e1f20-3a4b-4c6d-8e9f-0a1b2c3d4e5f",
  "eventoId": "3c1f9d2e-8a4b-4f6c-9e1d-2b7a5c8e0f13",
  "categoria": "VIP",
  "precioPagado": 250000,
  "estado": "ACTIVA",
  "fechaCompra": "2026-11-18T15:30:00Z",
  "codigoQr": "5d8e1f20-3a4b-4c6d-8e9f-0a1b2c3d4e5f.Qm9sZXRhRmlybWFkYUNvbkhNQUM"
}
```

**400** · **402** pago rechazado · **404** evento no está a la venta · **409** agotado · **422** evento cancelado, categoría que el evento no vende, o falta el carnet

`codigoQr` es **texto**, no una imagen. Para dibujarlo se usa una librería, por ejemplo `qrcode.react`:

```jsx
import { QRCodeSVG } from "qrcode.react";

<QRCodeSVG value={boleta.codigoQr} size={256} />
```

Ese mismo texto es el que se escanea en el check-in. Además, el comprador recibe el QR por correo (Mailpit en http://localhost:8025).

### `GET /api/boleteria/eventos/{eventoId}/aforo` · Aforo del evento (organizador)

Vendidas frente al aforo, por categoría (RF-05).

**200**
```json
{
  "eventoId": "3c1f9d2e-8a4b-4f6c-9e1d-2b7a5c8e0f13",
  "nombre": "Concierto de fin de semestre",
  "aforoMaximo": 500,
  "vendidas": 451,
  "porcentajeVendido": 90.2,
  "alertaEnviada": true,
  "aceptaVentas": true,
  "categorias": [
    { "categoria": "GENERAL", "cupos": 400, "disponibles": 0, "vendidas": 400, "precioBase": 80000, "tipoPrecio": "FIJO" },
    { "categoria": "VIP", "cupos": 100, "disponibles": 49, "vendidas": 51, "precioBase": 250000, "tipoPrecio": "FIJO" }
  ]
}
```

**404** si el evento no se ha publicado.

Para la pantalla de compra sirve también: `disponibles` dice qué categorías quedan, y `aceptaVentas` si el evento sigue a la venta. Ojo: aquí el campo se llama `categoria`, mientras que en Eventos se llama `tipo`.

### `POST /api/boleteria/checkin` · Check-in (staff)

El staff está en la entrada de **un** evento y valida el QR de cada asistente (RF-03).

```json
{
  "codigoQr": "5d8e1f20-3a4b-4c6d-8e9f-0a1b2c3d4e5f.Qm9sZXRhRmlybWFkYUNvbkhNQUM",
  "eventoId": "3c1f9d2e-8a4b-4f6c-9e1d-2b7a5c8e0f13",
  "staffId": "11111111-2222-3333-4444-555555555555"
}
```

**200** la persona entra:
```json
{ "aceptado": true, "motivo": null, "boletaId": "5d8e1f20-3a4b-4c6d-8e9f-0a1b2c3d4e5f", "categoria": "VIP" }
```

**409** no entra. Es una respuesta normal de negocio, con el mismo cuerpo y el motivo para mostrar:
```json
{ "aceptado": false, "motivo": "La boleta es de otro evento", "boletaId": "5d8e1f20-...", "categoria": null }
```

Motivos posibles: `QR inválido: fue alterado o no lo emitió EventHub`, `La boleta no existe`, `La boleta es de otro evento`, `La boleta ya fue usada`, `La boleta está cancelada`.

**400** si falta `codigoQr`, `eventoId` o `staffId`.

Una boleta de otro evento **no se marca como usada**: sigue sirviendo en la entrada de su propio evento.

---

## Qué endpoint usa cada pantalla

| Pantalla | Endpoints |
|---|---|
| Inicio / catálogo | `GET /api/eventos/catalogo` |
| Detalle y compra | `GET /api/eventos/{id}`, `GET /api/boleteria/eventos/{id}/aforo`, `POST /api/boleteria/boletas` |
| Mi boleta con QR | La respuesta de la compra (ver "Lo que todavía no existe") |
| Organizador: crear y editar | `POST /api/eventos`, `PUT /api/eventos/{id}` |
| Organizador: publicar, cancelar, reprogramar | `POST /api/eventos/{id}/publicar`, `/cancelar`, `PATCH /api/eventos/{id}` |
| Organizador: aforo | `GET /api/boleteria/eventos/{id}/aforo` |
| Check-in | `GET /api/eventos/{id}`, `POST /api/boleteria/checkin` |

## Lo que todavía no existe

- **"Mis boletas"**: no hay endpoint para listar las boletas de un comprador. Por ahora, guardar en el navegador la respuesta de cada compra.
- **"Mis eventos" del organizador**: el catálogo solo trae los publicados; no hay forma de listar los borradores. Por ahora, guardar el `id` que devuelve `POST /api/eventos`.
- **Notificaciones**: no tiene API; los correos se ven en Mailpit (http://localhost:8025).
- **Reservas**: no están implementadas.

## Corrección pendiente en `frontend/nginx.conf`

Con la línea actual de Boletería, Nginx reenvía la ruta completa: `/api/boleteria/boletas` llega como `/api/boleteria/boletas` en vez de `/boletas`, y responde 404. Debe quedar con barra al final en las dos partes:

```nginx
location /api/eventos     { proxy_pass http://eventos:8080/eventos; }
location /api/boleteria/  { proxy_pass http://boleteria:8080/; }
location /                { try_files $uri /index.html; }
```

Comprobación, con todo levantado en Docker:

```bash
curl -i http://localhost:8080/api/boleteria/health   # 200 con JSON
```
