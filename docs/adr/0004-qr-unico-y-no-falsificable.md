# ADR-0004: Diseño del código QR único y no falsificable

**Estado:** Aceptado
**Fecha:** 2026-11-09
**Decisores:** Carlos Vargas, David Murcia, Karol Esquivel
**Responsable:** Carlos Vargas
**Requerimientos que atiende:** RNF-03 (Seguridad), RF-03 (Check-in), RF-04 (Confirmación con QR)
**Issues relacionados:** #13, #24, #25, #41

## Contexto

Cada boleta lleva un código QR que el staff escanea en la entrada. Hay dos amenazas distintas:

1. **Falsificación:** alguien inventa o modifica un QR para entrar sin pagar. Lo cubre el RNF-03.
2. **Reutilización:** alguien comparte la foto de una boleta real y entran dos personas. Lo cubre el RF-03.

Ningún mecanismo resuelve ambas por sí solo: una firma digital impide inventar códigos, pero una boleta firmada sigue siendo válida si se escanea dos veces.

## Decisión

El QR contiene un **token firmado con HMAC-SHA256**:

```
<boletaId>.<firma>
firma = HMAC-SHA256(clave_secreta, boletaId)
```

- `boletaId` es un **UUID aleatorio** generado por Boletería.
- La **clave secreta** solo la conoce Boletería y se carga desde una variable de entorno, nunca desde el código.

La validación en el check-in tiene dos pasos:

1. **Integridad:** Boletería recalcula la firma y la compara. Si no coincide, el QR se rechaza **sin consultar la base de datos**.
2. **Uso único:** se marca la boleta como usada con una actualización condicional atómica:

```sql
UPDATE boleta SET estado = 'USADA'
WHERE id = :boletaId AND estado = 'ACTIVA';
```

Si afecta 0 filas, la boleta ya fue usada, fue cancelada o no existe, y se rechaza. Si dos staff escanean la misma boleta al mismo tiempo, solo uno tiene éxito.

## Opciones consideradas

### Opción A: QR con el ID secuencial de la boleta

| Dimensión | Evaluación |
|---|---|
| Complejidad | Muy baja |
| Seguridad | Muy baja: el siguiente número es predecible |

**Desventajas:** cualquiera puede generar el QR de la boleta 1001 sabiendo que tiene la 1000. Incumple RNF-03. Descartada.

### Opción B: UUID aleatorio sin firma

| Dimensión | Evaluación |
|---|---|
| Complejidad | Baja |
| Seguridad | Alta: adivinar un UUID v4 es prácticamente imposible |
| Rendimiento | Cada lectura, incluso falsa, consulta la base |

**Ventajas:** simple y razonablemente seguro.
**Desventajas:** no hay forma de verificar la integridad sin ir a la base; un ataque con códigos basura genera carga innecesaria.

### Opción C: JWT firmado con datos de la boleta

| Dimensión | Evaluación |
|---|---|
| Complejidad | Media |
| Seguridad | Alta |
| Rendimiento | QR más grande y difícil de leer con cámaras de baja calidad |

**Ventajas:** estándar conocido; puede llevar evento, categoría y fecha.
**Desventajas:** el QR crece mucho; los datos viajan legibles (solo firmados, no cifrados).

### Opción D: UUID + firma HMAC + actualización condicional ✅

| Dimensión | Evaluación |
|---|---|
| Complejidad | Baja-media |
| Seguridad | Alta contra falsificación y reutilización |
| Rendimiento | Los códigos falsos se rechazan sin tocar la base |

**Ventajas:** cubre las dos amenazas; QR corto; usa librerías estándar de Java (`javax.crypto.Mac`).
**Desventajas:** si la clave secreta se filtra, se pueden generar QR válidos; hay que proteger y poder rotar la clave.

## Análisis del trade-off

La firma resuelve la **integridad** (RNF-03) y la actualización condicional resuelve el **uso único** (RF-03). Separar los dos problemas permite usar para cada uno el mecanismo más simple que lo resuelve. Frente al JWT, se sacrifica llevar datos dentro del QR, pero el check-in igual necesita consultar la base para el estado de la boleta, así que esos datos no aportaban.

## Consecuencias

- **Más fácil:** probar las dos amenazas por separado (QR alterado → rechazado; QR válido escaneado dos veces → rechazado la segunda).
- **Más difícil:** gestionar la clave secreta; no puede quedar en el repositorio.
- **Limitación:** el check-in requiere conexión con Boletería; no funciona sin red en la entrada del evento.
- **Revisar:** si se implementa el PDF de boleta (bonificación), el QR embebido debe ser exactamente este token.

## Acciones

1. [ ] Implementar la generación del token al confirmar la compra (#24).
2. [ ] Incluir el token en el mensaje `boleta.comprada` para que Notificaciones genere la imagen del QR (#15, #25).
3. [ ] Implementar la validación en dos pasos en el check-in (#24).
4. [ ] Tests: QR alterado, QR inventado, QR válido usado dos veces y dos lecturas simultáneas (#27).
5. [ ] Cargar la clave secreta por variable de entorno en docker-compose y documentarla en el README (#8, #30).
