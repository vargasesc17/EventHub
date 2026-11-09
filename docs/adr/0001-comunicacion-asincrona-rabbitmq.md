# ADR-0001: Comunicación asíncrona entre microservicios con RabbitMQ

**Estado:** Propuesto
**Fecha:** 2026-11-09
**Decisores:** Carlos Vargas, David Murcia, Karol Esquivel
**Responsable:** David Murcia
**Requerimientos que atiende:** RNF-02 (Disponibilidad), RF-04 (Notificaciones por correo), RF-05 (Alerta de aforo)
**Issues relacionados:** #13, #15, #25, #26

## Contexto

EventHub tiene tres microservicios (Eventos, Boletería y Notificaciones) que necesitan avisarse cuando algo pasa: se compró una boleta, cambió un evento o el aforo superó el 90 %.

El RNF-02 exige que **la compra se complete aunque Notificaciones esté caído**, y que al recuperarse envíe las notificaciones pendientes **sin perder ninguna**. Si Boletería llamara a Notificaciones de forma síncrona, una caída de Notificaciones haría fallar la compra.

Además, el enunciado del proyecto exige RabbitMQ para los flujos `boleta.comprada`, `evento.actualizado` y `evento.aforo.critico`. Este ADR documenta por qué esa restricción tiene sentido técnico y **cómo** se va a usar.

## Decisión

Los microservicios se comunican de forma **asíncrona mediante RabbitMQ** para los tres eventos de dominio. Las llamadas síncronas (HTTP/REST) se reservan para consultas donde se necesita la respuesta en el momento.

Configuración acordada:

- Un **exchange de tipo `topic`** llamado `eventhub.events`.
- Routing keys iguales al nombre del evento: `boleta.comprada`, `evento.actualizado`, `evento.aforo.critico`.
- **Una cola durable por consumidor** (por ejemplo, `notificaciones.boleta-comprada`), para que cada servicio reciba su propia copia.
- **Mensajes persistentes** (`delivery_mode = 2`), para que sobrevivan a un reinicio del broker.
- **Confirmación manual (`ack`)**: el consumidor confirma el mensaje solo después de procesarlo. Si falla, el mensaje vuelve a la cola.
- **Dead Letter Queue (DLQ)** para los mensajes que fallen después de 3 reintentos, para no bloquear la cola.

## Opciones consideradas

### Opción A: llamadas HTTP síncronas entre servicios

| Dimensión | Evaluación |
|---|---|
| Complejidad | Baja |
| Costo | Ninguno adicional |
| Escalabilidad | Baja: un servicio lento frena a todos los que lo llaman |
| Familiaridad del equipo | Alta |

**Ventajas:** simple de implementar y depurar; no hay infraestructura adicional.
**Desventajas:** acopla la disponibilidad de los servicios. Si Notificaciones cae, la compra falla, lo que **incumple el RNF-02**. Boletería tendría que conocer a todos los interesados en sus eventos.

### Opción B: RabbitMQ (message broker) ✅

| Dimensión | Evaluación |
|---|---|
| Complejidad | Media |
| Costo | Un contenedor adicional en docker-compose |
| Escalabilidad | Alta para el volumen de EventHub |
| Familiaridad del equipo | Media: se vio en el curso |

**Ventajas:** desacopla en el tiempo (el consumidor puede estar caído) y en el espacio (el publicador no sabe quién consume). Permite agregar nuevos consumidores sin tocar al publicador.
**Desventajas:** consistencia eventual; más difícil de depurar; hay que diseñar reintentos y mensajes duplicados.

### Opción C: Apache Kafka

| Dimensión | Evaluación |
|---|---|
| Complejidad | Alta |
| Costo | Mayor consumo de recursos (broker + coordinación) |
| Escalabilidad | Muy alta |
| Familiaridad del equipo | Baja |

**Ventajas:** guarda el historial de eventos y permite reprocesarlos; maneja volúmenes enormes.
**Desventajas:** sobredimensionado para el volumen de EventHub y fuera del alcance del curso.

## Análisis del trade-off

El atributo de calidad que define esta decisión es la **disponibilidad** (RNF-02). La opción A es más simple, pero no puede cumplirlo. Entre B y C, Kafka resuelve problemas de volumen que EventHub no tiene. RabbitMQ ofrece exactamente lo necesario: entrega garantizada con colas durables y confirmación manual, con menor complejidad operativa.

El precio es aceptar **consistencia eventual**: el correo de confirmación puede tardar unos segundos después de la compra. Para EventHub es aceptable, porque la compra en sí ya quedó confirmada.

## Consecuencias

- **Más fácil:** la compra no depende de Notificaciones; agregar un nuevo consumidor (por ejemplo, un servicio de analítica) no requiere cambiar a Boletería.
- **Más difícil:** depurar un flujo que cruza tres servicios; probar escenarios de fallo.
- **Riesgo conocido:** si Boletería guarda la compra en su base de datos y **falla justo antes de publicar** el mensaje, el mensaje se pierde. La solución estándar es el patrón **Transactional Outbox** (guardar el mensaje en la misma transacción y publicarlo después). Por alcance del proyecto se deja como mejora futura y se menciona en la sustentación.
- **Los consumidores deben ser idempotentes:** con `ack` manual, un mensaje puede llegar dos veces. Cada mensaje lleva un `messageId` y el consumidor ignora los que ya procesó.

## Acciones

1. [ ] Definir exchange, colas y routing keys en `/docs/contratos/` (#15).
2. [ ] Configurar RabbitMQ en docker-compose (#8).
3. [ ] Implementar consumidores con `ack` manual e idempotencia (#25, #26).
4. [ ] Probar la compra con Notificaciones apagado (#28).
