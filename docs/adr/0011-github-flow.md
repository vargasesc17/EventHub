# ADR-0011: GitHub Flow como flujo de ramas (no Gitflow)

**Estado:** Aceptado
**Fecha:** 2026-11-09 (guía de contribución, PR #43)
**Documentado:** 2026-11-19
**Decisores:** Carlos Vargas, David Murcia, Karol Esquivel
**Responsable:** Carlos Vargas
**Requerimientos que atiende:** requisito de trabajo colaborativo con Git del enunciado
**Issues relacionados:** #7

## Contexto

Tres personas trabajan en paralelo en tres servicios y un frontend durante dos semanas, con una sola entrega final. El profesor revisa el historial para evaluar el trabajo en equipo, así que cada cambio debe quedar trazable a un issue, a un autor y a una revisión.

## Decisión

Se usa **GitHub Flow**:

1. `main` es la única rama permanente y siempre debe compilar. Está protegida: no se puede hacer push directo.
2. Cada issue tiene su rama corta desde `main`: `tipo/N-descripcion` (`feat/`, `fix/`, `docs/`, `infra/`).
3. Commits con **Conventional Commits** (`feat(boleteria): ...`).
4. Todo cambio entra por **pull request con al menos una aprobación** de otro integrante.
5. Merge con **squash**: un commit por PR en `main`, con el número del PR en el mensaje. Luego se borra la rama.

El detalle está en [`CONTRIBUTING.md`](../../CONTRIBUTING.md).

## Opciones consideradas

### Opción A: GitHub Flow ✅

| Dimensión | Evaluación |
|---|---|
| Complejidad | Baja |
| Costo | Ninguno |
| Escalabilidad | Suficiente para un equipo pequeño |
| Familiaridad del equipo | Media: dos integrantes aprendían Git |

**Ventajas:** una sola rama que entender; ciclos cortos; el historial de `main` se lee como una lista de PR.
**Desventajas:** no separa "lo que está en desarrollo" de "lo que se entrega"; `main` debe mantenerse estable siempre.

### Opción B: Gitflow (`main`, `develop`, `feature/*`, `release/*`, `hotfix/*`)

| Dimensión | Evaluación |
|---|---|
| Complejidad | Alta |
| Costo | Ninguno |
| Escalabilidad | Alta para varias versiones en paralelo |
| Familiaridad del equipo | Baja |

**Ventajas:** pensado para productos con varias versiones publicadas a la vez.
**Desventajas:** hay una sola entrega y ninguna versión en producción que mantener; `develop` y `release/*` serían ramas vacías de propósito y más merges que resolver.

### Opción C: trunk-based (commits directos a `main`)

**Desventajas:** sin revisión ni evidencia de colaboración. Descartada.

## Análisis del trade-off

Gitflow resuelve un problema que el proyecto no tiene (versiones paralelas). GitHub Flow da lo que sí se necesita: revisión obligatoria, trazabilidad issue → rama → PR → commit, y un `main` siempre entregable.

## Consecuencias

- **Más fácil:** cada commit de `main` lleva el número de su PR (`(#65)`), así que el historial muestra quién hizo qué.
- **Más difícil:** el squash rompe las ramas que partieron de una versión vieja de `main`. Pasó con la rama del frontend de Karol, que se rescató como #73 con `Co-authored-by` para conservar la autoría, y con un rebase de la rama de #24 que hubo que rehacer desde `main`.
- **Riesgo:** un PR aprobado sin revisar a fondo rompe `main`; se mitiga con `mvn test` antes de pedir revisión.

## Acciones

1. [x] `CONTRIBUTING.md` y plantilla de PR (#43).
2. [x] Protección de `main` con una aprobación obligatoria.
3. [x] Diagrama del flujo de ramas y tabla de participación en el documento final (sección B.4).
