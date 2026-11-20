# ADR-0009: Errores de la API con ProblemDetail (RFC 9457)

**Estado:** Aceptado
**Fecha:** 2026-11-15 (Eventos, #57); 2026-11-17 (Boletería, #65)
**Documentado:** 2026-11-19
**Decisores:** Carlos Vargas, David Murcia, Karol Esquivel
**Responsable:** Carlos Vargas
**Requerimientos que atiende:** RF-01, RF-02, RF-03, RNF-01 (el rechazo por falta de cupo debe ser claro)
**Issues relacionados:** #19–#23

## Contexto

El frontend necesita distinguir por qué falla una operación para mostrar el mensaje correcto: no es lo mismo "el evento no existe" que "no quedan cupos" o "un estudiante debe enviar su carné". Si cada controlador arma su propio JSON de error, el frontend tiene que conocer varios formatos, y algunas excepciones terminarían como un 500 genérico.

## Decisión

- Todas las respuestas de error usan **`ProblemDetail`** de Spring (formato estándar RFC 9457: `type`, `title`, `status`, `detail`).
- Cada servicio HTTP tiene **un solo `@RestControllerAdvice`** (`ManejadorDeErrores`, en el paquete `interfaces`) que traduce excepciones del dominio y de la aplicación a códigos HTTP. Los controladores no capturan excepciones.
- Convención de códigos:

| Código | Cuándo | Ejemplo |
|---|---|---|
| 400 | La petición está mal formada o le faltan campos | `categoria: "PALCO"`; campo obligatorio vacío (se devuelve la lista `errores`) |
| 404 | El recurso no existe | Evento inexistente |
| 409 | Conflicto con el estado actual | Sin cupos; boleta ya usada; QR de otro evento |
| 422 | Viola una regla de negocio | Evento en el pasado; estudiante sin carné; comprar en evento cancelado; cortesía con precio |
| 402 | El pago fue rechazado (pasarela simulada) | `PagoRechazadoException` |

## Opciones consideradas

### Opción A: ProblemDetail + `@RestControllerAdvice` ✅

| Dimensión | Evaluación |
|---|---|
| Complejidad | Baja |
| Costo | Ninguno: viene en Spring |
| Escalabilidad | Alta |
| Familiaridad del equipo | Media |

**Ventajas:** formato estándar; un solo lugar por servicio; el dominio lanza excepciones propias sin conocer HTTP (ADR-0007).
**Desventajas:** hay que mantener la tabla excepción → código.

### Opción B: DTO de error propio (`{ "mensaje": ..., "codigo": ... }`)

| Dimensión | Evaluación |
|---|---|
| Complejidad | Baja |
| Costo | Ninguno |
| Escalabilidad | Media |
| Familiaridad del equipo | Alta |

**Desventajas:** reinventa un estándar que ya existe y que Swagger documenta solo.

### Opción C: `try/catch` en cada controlador con `ResponseEntity`

**Desventajas:** código repetido; fácil olvidar un caso y devolver 500. Descartada.

## Análisis del trade-off

El estándar cuesta lo mismo que el formato propio y además lo entiende cualquier cliente. Separar 409 (estado) de 422 (regla) le permite al frontend decidir si reintentar o mostrar el error.

## Consecuencias

- **Más fácil:** la colección de Postman prueba cada caso de error por su código (422 evento en el pasado, 409 cortesía agotada, 409 QR alterado).
- **Más fácil:** el frontend muestra `detail` directamente al usuario.
- **Más difícil:** el check-in devuelve 200 o 409 con un cuerpo de resultado (aceptado/rechazado y motivo) en lugar de excepción, porque el staff necesita el motivo en pantalla.
- **Riesgo:** una excepción nueva sin manejador se convierte en 500; se mitiga con pruebas de controlador (`*ControllerTest`).

## Acciones

1. [x] `ManejadorDeErrores` en Eventos (#57) y Boletería (#65).
2. [x] Pruebas de controlador para 400/404/409/422.
3. [x] Respuestas de error documentadas en Swagger (ADR-0014).
