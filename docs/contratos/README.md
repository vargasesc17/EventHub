# Contratos de mensajes RabbitMQ

**Estado:** Propuesto, pendiente de aprobación de Carlos, David y Karol (#15)
**Responsable:** David Murcia
**Decisiones en las que se basa:** [ADR-0001](../adr/0001-comunicacion-asincrona-rabbitmq.md) (RabbitMQ), [ADR-0003](../adr/0003-control-de-concurrencia-y-dueno-del-aforo.md) (aforo), [ADR-0004](../adr/0004-qr-unico-y-no-falsificable.md) (QR), [ADR-0006](../adr/0006-replica-de-compradores-en-notificaciones.md) (copia de compradores)

Este documento fija **el formato de los mensajes antes de programar**, para que cada uno implemente su servicio en paralelo sin esperar a los demás. Si un servicio necesita cambiar un contrato, se hace por PR aquí primero.

## Resumen

| Mensaje | Publica | Consume | Para qué |
|---|---|---|---|
| `boleta.comprada` | Boletería | Notificaciones | Enviar la confirmación con el QR y guardar el comprador en la copia local (RF-04, ADR-0006) |
| `evento.actualizado` | Eventos | Notificaciones, Boletería | Alertar a los compradores (RF-04); crear o ajustar el inventario en Boletería (ADR-0003) |
| `evento.aforo.critico` | Boletería | Notificaciones | Alertar al organizador cuando las ventas alcanzan o superan el 90 % (RF-05) |

## Topología

```
                         exchange: eventhub.events (topic, durable)
                                        │
   ┌────────────────────────────────────┼─────────────────────────────────────┐
   │ boleta.comprada                    │ evento.actualizado                  │ evento.aforo.critico
   ▼                                    ▼                                     ▼
notificaciones.boleta-comprada   notificaciones.evento-actualizado   notificaciones.aforo-critico
                                 boleteria.evento-actualizado
```

### Exchange

| Nombre | Tipo | Durable |
|---|---|---|
| `eventhub.events` | `topic` | Sí |
| `eventhub.dlx` | `direct` | Sí (dead letter exchange) |

### Colas

Una cola durable **por consumidor**: si dos servicios escuchan el mismo mensaje, cada uno recibe su propia copia.

| Cola | Routing key (binding) | Servicio que la consume | Cola de mensajes muertos |
|---|---|---|---|
| `notificaciones.boleta-comprada` | `boleta.comprada` | Notificaciones | `notificaciones.boleta-comprada.dlq` |
| `notificaciones.evento-actualizado` | `evento.actualizado` | Notificaciones | `notificaciones.evento-actualizado.dlq` |
| `notificaciones.aforo-critico` | `evento.aforo.critico` | Notificaciones | `notificaciones.aforo-critico.dlq` |
| `boleteria.evento-actualizado` | `evento.actualizado` | Boletería | `boleteria.evento-actualizado.dlq` |

Cada cola se declara con estos argumentos para que los mensajes fallidos terminen en su DLQ:

```
x-dead-letter-exchange    = eventhub.dlx
x-dead-letter-routing-key = <nombre-de-la-cola>.dlq
```

**Quién declara qué:** el **publicador** declara el exchange; el **consumidor** declara sus colas, sus DLQ y los bindings al arrancar. Así cada servicio puede arrancar en cualquier orden.

## Formato común (sobre)

Todos los mensajes tienen la misma estructura exterior. Lo que cambia entre mensajes es el contenido de `datos`.

```json
{
  "messageId": "7f3c2a10-5b1e-4c8e-9a2f-1d6b8e4c0a11",
  "tipo": "boleta.comprada",
  "version": 1,
  "fechaEmision": "2026-11-15T19:30:00Z",
  "origen": "boleteria",
  "datos": { }
}
```

| Campo | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| `messageId` | UUID | Sí | Identificador único del mensaje. El consumidor lo usa para **ignorar duplicados** (idempotencia, ADR-0001). |
| `tipo` | texto | Sí | Igual a la routing key. |
| `version` | entero | Sí | Versión del contrato. Empieza en `1`; sube si se hace un cambio que rompe a los consumidores. |
| `fechaEmision` | fecha ISO-8601 en UTC | Sí | Cuándo se publicó. |
| `origen` | `eventos` \| `boleteria` | Sí | Servicio que publicó el mensaje. |
| `datos` | objeto | Sí | Contenido propio de cada mensaje (ver abajo). |

**Convenciones:** nombres de campos en `camelCase` y en español; fechas en ISO-8601 UTC; dinero en pesos colombianos como número entero (sin decimales); IDs como UUID en texto. El cuerpo se envía como JSON con `content_type = application/json`.

## 1. `boleta.comprada`

**Publica:** Boletería, después de confirmar el pago de una compra (RF-01). **No** se publica al crear una reserva, solo cuando se paga.
**Consume:** Notificaciones → envía la confirmación con el QR (#25) y guarda el comprador en su copia local (ADR-0006).

| Campo de `datos` | Tipo | Descripción |
|---|---|---|
| `boletaId` | UUID | ID de la boleta. |
| `eventoId` | UUID | ID del evento. |
| `categoria` | `GENERAL` \| `VIP` \| `ESTUDIANTE` \| `CORTESIA` | Tipo de boleta. |
| `precioPagado` | entero ≥ 0 | Precio final en COP (ya aplicada la estrategia de precio). |
| `fechaCompra` | fecha ISO-8601 | Cuándo se pagó. |
| `codigoQr` | texto | Token firmado `<boletaId>.<firma>` según ADR-0004. Notificaciones lo convierte en imagen QR; **no** lo valida. |
| `comprador.compradorId` | UUID | ID del comprador. |
| `comprador.nombre` | texto | Para el saludo del correo. |
| `comprador.correo` | correo | **Obligatorio por ADR-0006**: Notificaciones lo guarda para avisar cambios del evento. |
| `evento.nombre` | texto | Copia del nombre para el correo, así Notificaciones no tiene que consultar a Eventos. |
| `evento.fechaInicio` | fecha ISO-8601 | Para el correo y para programar el **recordatorio 24 horas antes** (RF-04). |
| `evento.lugar` | texto | Para el correo. |

Esquema: [`boleta.comprada.schema.json`](boleta.comprada.schema.json) · Ejemplo: [`ejemplos/boleta.comprada.json`](ejemplos/boleta.comprada.json)

## 2. `evento.actualizado`

**Publica:** Eventos, cuando un evento se publica, cambia de fecha/hora, de lugar o de cupos, o se cancela (RF-02, ADR-0003). Si en una sola operación cambian varias cosas, se publica **un solo mensaje** con todas en `cambios`, para que cada comprador reciba una sola alerta (RF-04: "cada uno una sola vez").

**Valores de `cambios`:**

| Valor | Cuándo | ¿Se combina? |
|---|---|---|
| `PUBLICADO` | El evento pasa de borrador a publicado | No: va solo |
| `FECHA` | Cambia la fecha o la hora | Sí, con `LUGAR` e `INVENTARIO` |
| `LUGAR` | Cambia el lugar | Sí, con `FECHA` e `INVENTARIO` |
| `INVENTARIO` | Cambia `aforoMaximo` o los cupos de alguna categoría | Sí, con `FECHA` y `LUGAR` |
| `CANCELADO` | El evento se cancela | No: va solo |

**Consumen:**
- **Notificaciones** → alerta a todos los compradores del evento usando su copia local (#26) si `cambios` incluye `FECHA`, `LUGAR` o `CANCELADO`. **Ignora** los mensajes que solo traen `PUBLICADO` (todavía no hay compradores) o solo `INVENTARIO` (no afecta a quien ya compró). Si cambia `FECHA`, también reprograma el recordatorio de 24 horas.
- **Boletería** → actúa con `PUBLICADO` (crea el inventario por categoría y guarda el aforo máximo y el organizador), `INVENTARIO` (ajusta el inventario, ver abajo) y `CANCELADO` (bloquea nuevas ventas). Ignora `FECHA` y `LUGAR`.

| Campo de `datos` | Tipo | Descripción |
|---|---|---|
| `eventoId` | UUID | ID del evento. |
| `cambios` | lista de `PUBLICADO` \| `FECHA` \| `LUGAR` \| `INVENTARIO` \| `CANCELADO` | Qué cambió en esta operación, sin repetidos. `FECHA` incluye cambios de hora. |
| `estado` | `PUBLICADO` \| `EN_CURSO` \| `FINALIZADO` \| `CANCELADO` | Estado del evento después del cambio. Con `PUBLICADO` debe ser `PUBLICADO`; con `CANCELADO` debe ser `CANCELADO`. |
| `nombre` | texto | Nombre del evento. |
| `fechaInicio` | fecha ISO-8601 | Fecha y hora **actuales** (después del cambio). |
| `lugar` | texto | Lugar **actual**. |
| `valoresAnteriores` | objeto o `null` | Trae lo anterior de `FECHA` (`fechaInicio`) y/o de `LUGAR` (`lugar`). Si `cambios` no incluye ninguno de los dos, va en `null`. Sirve para el texto del correo ("cambió del 28 al 30 de noviembre"). |
| `aforoMaximo` | entero > 0 | Aforo total del evento (ADR-0003). |
| `organizador.organizadorId` | UUID | ID del organizador. |
| `organizador.correo` | correo | Boletería lo guarda para incluirlo en `evento.aforo.critico`. |
| `categorias[]` | lista | Datos del inventario: una entrada por categoría. |
| `categorias[].categoria` | `GENERAL` \| `VIP` \| `ESTUDIANTE` \| `CORTESIA` | |
| `categorias[].cupos` | entero ≥ 0 | Cupos totales de la categoría. |
| `categorias[].precioBase` | entero ≥ 0 | Precio base en COP; Boletería aplica la estrategia de precio encima. |

`aforoMaximo` y `categorias` siempre traen los valores **completos y actuales**, no la diferencia. Igual el resto de campos: cada mensaje describe el estado completo del evento.

**Cómo aplica Boletería un cambio de `INVENTARIO`:**
- **Sube un cupo:** suma la diferencia a los disponibles.
- **Baja un cupo:** resta la diferencia sin bajar de 0. Nunca anula boletas vendidas o reservadas.
- **Categoría nueva:** crea su inventario.
- **Categoría que desaparece:** sus disponibles quedan en 0 y las boletas ya vendidas siguen siendo válidas.
- **Cambia `aforoMaximo`:** recalcula el umbral del 90 %. Si la alerta ya se envió, no se reenvía.

Esa protección vive en Boletería porque Eventos no conoce las ventas (ADR-0003).

Esquema: [`evento.actualizado.schema.json`](evento.actualizado.schema.json) · Ejemplos: [`publicado`](ejemplos/evento.actualizado.publicado.json), [`fecha-lugar`](ejemplos/evento.actualizado.fecha-lugar.json), [`inventario`](ejemplos/evento.actualizado.inventario.json)

## 3. `evento.aforo.critico`

**Publica:** Boletería, en la compra que hace que las boletas vendidas **alcancen o superen** el 90 % del aforo, es decir, cuando `vendidas * 100 >= aforoMaximo * 90` (el mismo `>=` del SQL de ADR-0003). Se publica **una sola vez por evento**.
**Consume:** Notificaciones → envía la alerta al organizador (#26).

| Campo de `datos` | Tipo | Descripción |
|---|---|---|
| `eventoId` | UUID | ID del evento. |
| `nombreEvento` | texto | Para el correo. |
| `organizador.organizadorId` | UUID | ID del organizador. |
| `organizador.correo` | correo | A quién se envía la alerta. Boletería lo recibió en `evento.actualizado` (`PUBLICADO`). |
| `aforoMaximo` | entero > 0 | Aforo total. |
| `vendidas` | entero ≥ 0 | Boletas vendidas en el momento de la alerta. |
| `porcentajeVendido` | número 0–100 | `vendidas * 100 / aforoMaximo`, con un decimal. |
| `umbral` | entero 1–100 | Umbral configurado (hoy 90). Se envía porque ADR-0003 pide que sea configurable. |

Esquema: [`evento.aforo.critico.schema.json`](evento.aforo.critico.schema.json) · Ejemplo: [`ejemplos/evento.aforo.critico.json`](ejemplos/evento.aforo.critico.json)

## Qué pasa si el consumidor está caído o falla

Estas reglas son las que hacen cumplir el **RNF-02** (la compra funciona aunque Notificaciones esté caído y, al volver, no se pierde ninguna notificación).

| Situación | Qué pasa | Configuración que lo garantiza |
|---|---|---|
| El consumidor está apagado | Los mensajes se quedan esperando en su cola. Al arrancar, los procesa en orden. | Colas `durable = true` |
| RabbitMQ se reinicia | Los mensajes no se pierden. | Exchange y colas durables + mensajes persistentes (`delivery_mode = 2`) |
| El consumidor se cae a mitad de procesar | El mensaje vuelve a la cola y se entrega otra vez. | `ack` **manual**: se confirma solo después de procesar con éxito |
| El mismo mensaje llega dos veces | Se ignora la segunda vez. | El consumidor guarda los `messageId` procesados (tabla `mensajes_procesados`) |
| El procesamiento falla (por ejemplo, error al enviar el correo) | Se reintenta **3 veces** con espera de 1 s, 2 s y 4 s. | Reintentos en el consumidor (Spring Retry) |
| Falla después de los 3 reintentos | Se rechaza sin reencolar y va a su DLQ para revisarlo a mano. No bloquea la cola. | `basicReject(requeue=false)` + `x-dead-letter-exchange` |
| El JSON no cumple el contrato | Va directo a la DLQ, sin reintentos (reintentar no lo va a arreglar). | Validación al deserializar |
| Un `evento.actualizado` viejo llega después de uno nuevo (por los reintentos) | Se ignora el viejo. Como cada mensaje trae el estado completo, aplicar solo el más reciente basta. | El consumidor guarda la última `fechaEmision` aplicada por `eventoId` e ignora los mensajes con una `fechaEmision` anterior |
| El publicador no logra publicar | La operación de negocio ya quedó guardada; el mensaje se pierde. **Riesgo conocido**, se mitiga en el futuro con Transactional Outbox (ADR-0001). | Publisher confirms para al menos registrar el error en el log |

**Prefetch:** cada consumidor usa `prefetch = 10` para no cargar demasiados mensajes en memoria.

## Reglas para cambiar un contrato

- **Agregar un campo opcional** no rompe a nadie: se puede hacer sin subir `version`. Los consumidores **deben ignorar los campos que no conocen**.
- **Quitar o renombrar un campo, o cambiar su tipo** rompe a los consumidores: se sube `version` y se avisa a los dueños de los servicios que consumen el mensaje.

## Qué valida el esquema y qué no

JSON Schema revisa cada campo por separado, pero **no compara campos entre sí**. Esas reglas las garantiza el servicio que publica:
- **Eventos** valida que no haya categorías repetidas y que la suma de `cupos` no supere `aforoMaximo`.
- **Boletería** garantiza que `vendidas <= aforoMaximo`.

## Decisiones tomadas en la revisión

1. **Varios cambios a la vez → un solo mensaje.** `tipoCambio` pasó a ser la lista `cambios`. Si cambian la fecha y el lugar juntos, Eventos publica un mensaje con `["FECHA", "LUGAR"]` y cada comprador recibe una sola alerta.
2. **`boleta.cancelada` queda fuera de esta entrega.** Dentro del alcance, ningún flujo cancela una boleta ya pagada. Queda como acción pendiente en [ADR-0006](../adr/0006-replica-de-compradores-en-notificaciones.md) y se menciona en la sustentación como mejora.

**Para #25 y #26 (implementación):** en Spring AMQP hay que poner `defaultRequeueRejected=false` (o lanzar `AmqpRejectAndDontRequeueException`). Si no, un mensaje que falla vuelve a la cola para siempre y nunca llega a la DLQ.

## Aprobación

- [ ] Carlos (Eventos y Boletería: publicador de los tres mensajes)
- [ ] Karol (frontend: revisa que los datos alcancen para las pantallas)
- [ ] David (Notificaciones: consumidor)
