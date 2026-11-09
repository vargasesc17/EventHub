# ADR-0005: Cómo accede el frontend a los microservicios

**Estado:** Propuesto — pendiente de decisión de Karol (responsable) y revisión de Carlos y David
**Fecha:** 2026-11-09
**Decisores:** Karol Esquivel, Carlos Vargas, David Murcia
**Responsable:** Karol Esquivel
**Requerimientos que atiende:** RF-01, RF-02, RF-03, RF-06 (todos se usan desde la aplicación web), RNF-03 (Seguridad), RNF-04 (Rendimiento)
**Issues relacionados:** #35, #36, #37, #38

## Contexto

La aplicación web (React) necesita llamar a los tres microservicios. Si el navegador los llama directamente, tiene que conocer tres direcciones distintas, y cada servicio debe configurar **CORS** (el navegador bloquea peticiones a un dominio distinto al de la página si el servidor no las autoriza).

Además, si se implementa la bonificación de autenticación JWT con roles (#32), hay que decidir **dónde se valida el token**: en cada servicio o en un único punto de entrada.

El equipo tiene hasta el 19 de noviembre, así que la simplicidad pesa mucho.

## Decisión (propuesta)

Usar **Nginx como punto único de entrada**, en el mismo contenedor que sirve la aplicación React:

- `/` → archivos estáticos de React.
- `/api/eventos/**` → microservicio Eventos.
- `/api/boleteria/**` → microservicio Boletería.

Como la página y la API se sirven desde el **mismo origen**, el navegador no aplica CORS. El frontend solo conoce rutas relativas (`/api/...`).

> **Nota sobre el diagrama C2:** el borrador de Contenedores muestra un "API Gateway [Spring Cloud Gateway]" separado de la aplicación web. Si se acepta este ADR, hay que actualizarlo: la Aplicación Web pasa a ser `[Contenedor: React + Nginx]` y asume el papel de gateway.

## Opciones consideradas

### Opción A: el frontend llama directamente a cada microservicio

| Dimensión | Evaluación |
|---|---|
| Complejidad | Baja al inicio, crece con cada servicio |
| Costo | Ninguno adicional |
| Seguridad | Baja: los servicios quedan expuestos a internet |
| Familiaridad del equipo | Alta |

**Ventajas:** nada que configurar en el medio.
**Desventajas:** CORS en cada servicio; el frontend se acopla a la ubicación de cada servicio; cada servicio debe validar la autenticación por su cuenta.

### Opción B: Nginx como reverse proxy junto al frontend ✅ (propuesta)

| Dimensión | Evaluación |
|---|---|
| Complejidad | Baja: un archivo de configuración |
| Costo | Ninguno: Nginx ya sirve el frontend |
| Seguridad | Media-alta: solo Nginx queda expuesto |
| Familiaridad del equipo | Media |

**Ventajas:** elimina CORS; un solo contenedor público; muy liviano.
**Desventajas:** no tiene lógica propia; validar JWT en Nginx exige módulos adicionales, así que con la bonificación cada servicio validaría el token.

### Opción C: API Gateway dedicado (Spring Cloud Gateway)

| Dimensión | Evaluación |
|---|---|
| Complejidad | Media: un servicio Spring más |
| Costo | Un contenedor adicional |
| Seguridad | Alta: puede validar JWT en un solo punto |
| Familiaridad del equipo | Media: es Java, como el backend |

**Ventajas:** centraliza la autenticación y permite agregar límites de peticiones; encaja con el stack del backend.
**Desventajas:** un servicio más que construir, configurar y desplegar con poco tiempo.

### Opción D: Backend for Frontend (BFF)

**Ventajas:** un backend hecho a la medida de cada pantalla.
**Desventajas:** se justifica cuando hay varios clientes (web y móvil) con necesidades distintas; EventHub tiene uno solo. Sobredimensionado.

## Análisis del trade-off

El factor decisivo es el **tiempo**: la opción B resuelve CORS y la exposición de servicios sin construir nada nuevo. La opción C solo gana si se implementa la bonificación de JWT, porque centraliza la validación del token.

Por eso la decisión depende de una pregunta que debe responder el equipo: **¿se va a implementar la bonificación de JWT (#32)?**

- Si **no**: opción B.
- Si **sí**: opción C, o la B con validación del token en cada servicio mediante una librería compartida.

## Consecuencias (si se acepta la opción B)

- **Más fácil:** el frontend usa rutas relativas y funciona igual en desarrollo y en docker-compose.
- **Más fácil:** un solo puerto expuesto en docker-compose.
- **Más difícil:** si se agrega JWT después, cada servicio debe validarlo.
- **Pendiente:** actualizar el diagrama de Contenedores (#35).

## Acciones

1. [ ] Karol decide entre B y C según el alcance de la bonificación (#36).
2. [ ] Configurar Nginx en el contenedor del frontend (#38).
3. [ ] Actualizar el C2 con la tecnología elegida (#35).
