# ADR-0002: Una base de datos por microservicio

**Estado:** Propuesto
**Fecha:** 2026-11-09
**Decisores:** Carlos Vargas, David Murcia, Karol Esquivel
**Responsable:** David Murcia
**Requerimientos que atiende:** RNF-02 (Disponibilidad), RNF-01 (Consistencia)
**Issues relacionados:** #8, #13, #16, #17, #18

## Contexto

EventHub se divide en tres microservicios con responsabilidades distintas. Cada uno maneja datos propios:

- **Eventos:** eventos, categorías de boleta y aforo máximo.
- **Boletería:** boletas, inventario por categoría, compradores y check-ins.
- **Notificaciones:** compradores por evento y registro de envíos (ver ADR-0006).

Hay que decidir si estos datos viven en una base compartida o separada. Si dos servicios comparten tablas, un cambio en el esquema de uno puede romper al otro sin que nadie lo note, y en la práctica dejan de ser independientes (un "monolito distribuido").

## Decisión

Cada microservicio es **dueño exclusivo de sus datos**. Ningún servicio lee ni escribe en la base de otro; si necesita un dato ajeno, lo pide por la API del servicio dueño o lo recibe por un evento de RabbitMQ.

Implementación práctica para el proyecto:

- **Un solo servidor PostgreSQL** en docker-compose, con **tres bases de datos lógicas** separadas: `eventos_db`, `boleteria_db` y `notificaciones_db`.
- **Un usuario de base de datos por servicio**, con permisos solo sobre su propia base. Así la separación no depende de la disciplina del equipo: el motor la impone.

## Opciones consideradas

### Opción A: una base de datos compartida

| Dimensión | Evaluación |
|---|---|
| Complejidad | Baja |
| Costo | Mínimo |
| Escalabilidad | Baja: todos compiten por la misma base |
| Familiaridad del equipo | Alta |

**Ventajas:** consultas con `JOIN` entre dominios; transacciones que abarcan varios servicios.
**Desventajas:** acopla los servicios por el esquema; un cambio en una tabla afecta a todos; contradice el principio de microservicios que evalúa el curso.

### Opción B: una base lógica por servicio en un mismo servidor ✅

| Dimensión | Evaluación |
|---|---|
| Complejidad | Media |
| Costo | Un solo contenedor de PostgreSQL |
| Escalabilidad | Media: suficiente para el proyecto |
| Familiaridad del equipo | Alta |

**Ventajas:** independencia de esquemas; aislamiento impuesto por permisos; un solo contenedor que administrar.
**Desventajas:** si cae el servidor de PostgreSQL, caen las tres bases.

### Opción C: un servidor de base de datos por servicio

| Dimensión | Evaluación |
|---|---|
| Complejidad | Media-alta |
| Costo | Tres contenedores de base de datos |
| Escalabilidad | Alta: cada base escala por separado |
| Familiaridad del equipo | Alta |

**Ventajas:** aislamiento total, incluso ante fallos de infraestructura; cada servicio podría usar otro motor.
**Desventajas:** más recursos y configuración, sin un beneficio visible en la escala del proyecto.

## Análisis del trade-off

La opción B conserva lo que importa del patrón *database per service*, que es la **independencia de esquemas y la propiedad de los datos**, sin el costo operativo de la opción C. Lo que se sacrifica es el aislamiento ante una caída del servidor, que en producción se resolvería pasando a la opción C sin cambiar el código de los servicios: solo cambia la cadena de conexión.

## Consecuencias

- **Más fácil:** cada integrante evoluciona el esquema de su servicio sin coordinarse con los demás; se puede migrar a la opción C sin reescribir código.
- **Más difícil:** no hay `JOIN` entre dominios. Por ejemplo, el panel del organizador (bonificación) tendrá que combinar datos de Eventos y Boletería por API.
- **Más difícil:** no hay transacciones entre servicios; la consistencia entre ellos es eventual (ver ADR-0001).
- **Revisar:** si se implementa la bonificación del panel, evaluar si conviene una vista de lectura alimentada por eventos.

## Acciones

1. [ ] Crear las tres bases y sus usuarios en el script de inicialización de docker-compose (#8).
2. [ ] Configurar la conexión de cada servicio a su base (#16, #17, #18).
3. [ ] Verificar que el usuario de Boletería no pueda leer `eventos_db`.
