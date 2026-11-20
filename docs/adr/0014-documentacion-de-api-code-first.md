# ADR-0014: Documentación de la API desde el código (springdoc) con contrato versionado

**Estado:** Aceptado
**Fecha:** 2026-11-18 (Swagger en Eventos y Boletería, #75)
**Documentado:** 2026-11-19
**Decisores:** Carlos Vargas, David Murcia, Karol Esquivel
**Responsable:** Carlos Vargas
**Requerimientos que atiende:** RF-01, RF-02, RF-03, RF-06 (contrato entre frontend y servicios)
**Issues relacionados:** #66, #75

## Contexto

El frontend (Karol) consume las APIs de Eventos y Boletería mientras Carlos y David las siguen cambiando. Una documentación escrita a mano en Markdown (#66) se desactualizó en pocos días: el frontend llamaba rutas o campos que ya no existían. Los mensajes de RabbitMQ ya tenían su contrato en JSON Schema (`docs/contratos/`), pero las APIs HTTP no.

## Decisión

- **Code-first:** la especificación OpenAPI se genera desde los controladores con `springdoc-openapi-starter-webmvc-ui`. Swagger UI queda disponible en cada servicio (`/swagger-ui.html` en los puertos 8081 y 8082).
- Las anotaciones (`@Operation`, `@ApiResponse`) documentan también las respuestas de error ProblemDetail (ADR-0009).
- **Contrato versionado:** la especificación generada se guarda en `docs/api/eventos.openapi.yaml` y `docs/api/boleteria.openapi.yaml`, para que los cambios del contrato aparezcan en el diff del PR.
- Notificaciones no expone API de negocio (solo `/health`): su contrato son los mensajes de `docs/contratos/`.

## Opciones consideradas

### Opción A: code-first con springdoc + YAML versionado ✅

| Dimensión | Evaluación |
|---|---|
| Complejidad | Baja |
| Costo | Una dependencia |
| Escalabilidad | Alta |
| Familiaridad del equipo | Media |

**Ventajas:** la documentación no se desactualiza porque sale del código; Swagger UI sirve para probar en vivo.
**Desventajas:** el YAML versionado hay que regenerarlo a mano cuando cambia un controlador.

### Opción B: contract-first (escribir el OpenAPI y generar el código)

| Dimensión | Evaluación |
|---|---|
| Complejidad | Alta |
| Costo | Plugin de generación y código generado en el repo |
| Escalabilidad | Alta |
| Familiaridad del equipo | Baja |

**Ventajas:** el contrato se acuerda antes de programar.
**Desventajas:** el código generado choca con la estructura de Clean Architecture (ADR-0007) y el equipo no conocía el generador.

### Opción C: documentación manual en Markdown

**Desventajas:** ya se probó (#66) y se desactualizó. Reemplazada por esta decisión.

## Análisis del trade-off

En un proyecto corto donde la API cambia a diario, la fuente de verdad debe ser el código. Versionar el YAML recupera la ventaja de contract-first que más importa al equipo: ver en el PR cuándo cambia el contrato.

## Consecuencias

- **Más fácil:** el frontend y Postman se guían por Swagger UI.
- **Más fácil:** en la sustentación se pueden mostrar los endpoints y sus errores sin abrir el código.
- **Más difícil:** las anotaciones agregan ruido a los controladores (#76 las movió a cada método).
- **Riesgo:** olvidar regenerar el YAML; Swagger UI en vivo siempre muestra la versión real.

## Acciones

1. [x] springdoc en Eventos y Boletería (#75).
2. [x] `docs/api/*.openapi.yaml` versionados.
3. [x] Colección de Postman alineada con Swagger (#30).
