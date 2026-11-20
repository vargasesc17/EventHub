# ADR-0012: Sesión simulada por rol, sin JWT, por alcance

**Estado:** Aceptado
**Fecha:** 2026-11-18 (pantallas del frontend, #73)
**Documentado:** 2026-11-19
**Decisores:** Carlos Vargas, David Murcia, Karol Esquivel
**Responsable:** Karol Esquivel
**Requerimientos que atiende:** RF-01, RF-02, RF-03, RF-06 (cada rol ve sus pantallas)
**Issues relacionados:** #32 (JWT, bono opcional), #34–#38

## Contexto

La aplicación tiene tres roles (organizador, comprador y staff) con pantallas distintas. Una autenticación real (registro, contraseñas, JWT, validación del token en los tres servicios) es un trabajo grande que el enunciado no exige; el JWT aparece como bono opcional (#32). El tiempo disponible se priorizó para los RNF obligatorios (concurrencia, QR, disponibilidad).

## Decisión

- El frontend ofrece una **pantalla de ingreso con tres usuarios de prueba**, uno por rol (`frontend/src/sesion/usuarios.js`).
- El usuario elegido se guarda en `localStorage` (`Sesion.jsx`) y el componente `SoloRol` redirige si alguien entra a una ruta que no es de su rol.
- Los servicios **no validan identidad**: reciben el `organizadorId` o `compradorId` en la petición.

## Opciones consideradas

### Opción A: sesión simulada en el frontend ✅

| Dimensión | Evaluación |
|---|---|
| Complejidad | Baja |
| Costo | Ninguno |
| Escalabilidad | No aplica |
| Familiaridad del equipo | Alta |

**Ventajas:** permite demostrar los tres flujos en minutos; no toca los servicios.
**Desventajas:** no es seguridad real; cualquiera puede llamar la API con otro id.

### Opción B: JWT emitido por un servicio de autenticación propio

| Dimensión | Evaluación |
|---|---|
| Complejidad | Alta |
| Costo | Un servicio y una base más |
| Escalabilidad | Alta |
| Familiaridad del equipo | Baja |

**Desventajas:** un cuarto microservicio y filtros de seguridad en los tres existentes, sin requerimiento obligatorio que lo pida.

### Opción C: proveedor externo (Keycloak, Auth0)

**Desventajas:** otra pieza de infraestructura en el `docker-compose` y configuración que nadie del equipo conocía. Descartada.

## Análisis del trade-off

Se cambia seguridad real por alcance: el esfuerzo fue a lo que el enunciado evalúa. Como Nginx ya centraliza el acceso (ADR-0005), agregar JWT después consistiría en validar el token en un solo punto, sin cambiar los servicios.

## Consecuencias

- **Más fácil:** la sustentación cambia de rol en un clic.
- **Más difícil:** los ids de prueba quedan fijos en el código del frontend.
- **Riesgo:** **no apto para producción.** Está registrado como limitación en el documento final.

## Acciones

1. [x] Pantalla de ingreso y `SoloRol` (#73).
2. [ ] JWT validado en Nginx o en un API Gateway (#32, trabajo futuro).
