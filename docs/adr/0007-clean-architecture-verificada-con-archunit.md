# ADR-0007: Clean Architecture en cada microservicio, verificada con ArchUnit

**Estado:** Aceptado
**Fecha:** 2026-11-12 (entra con el esqueleto de Eventos, #51)
**Documentado:** 2026-11-19
**Decisores:** Carlos Vargas, David Murcia, Karol Esquivel
**Responsable:** Carlos Vargas (Eventos, Boletería) y David Murcia (Notificaciones)
**Requerimientos que atiende:** todos los RF (estructura común), RNF-02 (cambiar un adaptador no toca el dominio)
**Issues relacionados:** #16, #17, #18 (PR #51, #54, #56)

## Contexto

El enunciado pide aplicar SOLID y patrones de diseño, y los tres microservicios los escriben personas distintas. Sin una estructura común, cada servicio quedaría organizado a su manera y la regla "el dominio no depende del framework" dependería solo de la disciplina de cada uno. Además, varias decisiones anteriores (ADR-0001 mensajería, ADR-0002 base por servicio, ADR-0004 firma del QR) suponen que la infraestructura se puede cambiar sin tocar las reglas de negocio.

## Decisión

Los tres servicios siguen la misma estructura de cuatro capas, una por paquete:

| Paquete | Contiene | Puede depender de |
|---|---|---|
| `domain` | Entidades, reglas de negocio, puertos (interfaces) | Nada: ni Spring, ni JPA, ni Jakarta |
| `application` | Casos de uso que orquestan el dominio | `domain` |
| `interfaces` | Controladores REST, DTO, manejador de errores | `application`, `domain` |
| `infrastructure` | Adaptadores: JPA, RabbitMQ, SMTP, HMAC, pasarela | Todo lo anterior |

La regla de dependencias **no se deja a la revisión manual**: cada servicio tiene un `ArquitecturaTest` con ArchUnit 1.5.1 que falla el build si `domain` importa Spring/JPA, si `application` usa `infrastructure` o `interfaces`, o si `interfaces` usa `infrastructure`.

## Opciones consideradas

### Opción A: arquitectura por capas clásica (controller → service → repository)

| Dimensión | Evaluación |
|---|---|
| Complejidad | Baja |
| Costo | Ninguno |
| Escalabilidad | Media |
| Familiaridad del equipo | Alta |

**Ventajas:** es lo que trae cualquier tutorial de Spring.
**Desventajas:** el dominio queda atado a las entidades JPA; cambiar la base o el broker toca la lógica de negocio. No muestra inversión de dependencias (DIP).

### Opción B: Clean Architecture sin verificación automática

| Dimensión | Evaluación |
|---|---|
| Complejidad | Media |
| Costo | Ninguno |
| Escalabilidad | Alta |
| Familiaridad del equipo | Media |

**Ventajas:** la misma separación de la opción C.
**Desventajas:** basta un `import` equivocado en un PR para romperla sin que nadie lo note.

### Opción C: Clean Architecture verificada con ArchUnit ✅

| Dimensión | Evaluación |
|---|---|
| Complejidad | Media |
| Costo | Una dependencia de pruebas |
| Escalabilidad | Alta |
| Familiaridad del equipo | Media: ArchUnit era nuevo para el equipo |

**Ventajas:** la arquitectura se comprueba en cada `mvn test`; el diagrama de clases y el código no se pueden separar.
**Desventajas:** más archivos (puerto + adaptador) por cada dependencia externa.

### Opción D: módulos Maven separados por capa

**Desventajas:** cuatro `pom.xml` por servicio; demasiada ceremonia para el tamaño del proyecto. Descartada.

## Análisis del trade-off

Se paga con más clases (cada dependencia externa tiene un puerto en `domain` y un adaptador en `infrastructure`) a cambio de que el dominio se pruebe sin Spring y de que los adaptadores se cambien sin tocar reglas de negocio. ArchUnit convierte la regla en una prueba, que es la única forma de mantenerla con tres personas escribiendo código en paralelo.

## Consecuencias

- **Más fácil:** las pruebas de casos de uso usan fakes en memoria (`BoletasEnMemoria`, `PasarelaFalsa`, `PublicadorEnMemoria`), sin base ni broker (ADR-0010).
- **Más fácil:** el error de #24 (una prueba de punta a punta en el paquete `interfaces` que usaba clases de infraestructura) lo detectó ArchUnit antes del merge; la prueba se movió al paquete raíz (`CheckInDePuntaAPuntaTest`).
- **Más difícil:** las entidades JPA (`*Entity`) son distintas de las del dominio y hay que mapearlas.
- **Riesgo:** ArchUnit revisa dependencias entre paquetes, no si la lógica quedó en la capa correcta; eso sigue siendo trabajo de la revisión de PR.

## Acciones

1. [x] Esqueleto con las cuatro capas en Eventos, Boletería y Notificaciones (#51, #54, #56).
2. [x] `ArquitecturaTest` en los tres servicios.
3. [x] Diagramas de clases por servicio alineados con los paquetes (#55, #71).
