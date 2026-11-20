# ADR-0013: Esquema con `ddl-auto: update` en lugar de Flyway (deuda técnica consciente)

**Estado:** Aceptado
**Fecha:** 2026-11-15 (primeras entidades JPA de Eventos, #57)
**Documentado:** 2026-11-19
**Decisores:** Carlos Vargas, David Murcia, Karol Esquivel
**Responsable:** David Murcia
**Requerimientos que atiende:** ninguno directamente; afecta la operación (ADR-0002)
**Issues relacionados:** #19, #21, #25

## Contexto

Cada servicio tiene su propia base de datos (ADR-0002). Durante las dos semanas de desarrollo las entidades cambiaron casi a diario (categorías, estados de la boleta, aforo, réplica de compradores). Había que decidir cómo crear y evolucionar las tablas.

## Decisión

Los tres servicios usan `spring.jpa.hibernate.ddl-auto: update`: Hibernate crea las tablas que faltan y agrega columnas nuevas al arrancar. No hay scripts de migración versionados. El script de `infra/postgres` solo crea las tres bases y sus usuarios.

**Se registra como deuda técnica**: antes de un entorno real habría que pasar a migraciones.

## Opciones consideradas

### Opción A: `ddl-auto: update` ✅

| Dimensión | Evaluación |
|---|---|
| Complejidad | Muy baja |
| Costo | Ninguno |
| Escalabilidad | Baja: no sirve para producción |
| Familiaridad del equipo | Alta |

**Ventajas:** cero archivos que mantener mientras el modelo cambia cada día.
**Desventajas:** no borra ni renombra columnas; no deja historial del esquema; dos versiones del servicio pueden dejar la base en un estado no reproducible.

### Opción B: Flyway con scripts `V1__...sql`

| Dimensión | Evaluación |
|---|---|
| Complejidad | Media |
| Costo | Un script por cambio de entidad |
| Escalabilidad | Alta |
| Familiaridad del equipo | Baja |

**Ventajas:** esquema versionado y reproducible.
**Desventajas:** con el modelo cambiando a diario, cada cambio de entidad exigía un script y un conflicto más en los PR.

### Opción C: `ddl-auto: create-drop`

**Desventajas:** borra los datos en cada reinicio; rompe la demo de "Notificaciones se cae y luego se pone al día" (RNF-02). Descartada.

## Análisis del trade-off

Se prioriza la velocidad de desarrollo sobre la trazabilidad del esquema, sabiendo que es una decisión temporal. El costo de corregirla después es bajo: Flyway puede generar la línea base a partir del esquema actual.

## Consecuencias

- **Más fácil:** agregar un campo a una entidad no requiere tocar nada más.
- **Más difícil:** si se renombra un campo, la columna vieja queda en la base. Para empezar limpio hay que borrar el volumen: `docker compose down -v`.
- **Riesgo:** en producción, un cambio de entidad podría dejar el esquema inconsistente. **Deuda técnica**, listada en el documento final.

## Acciones

1. [x] `ddl-auto: update` en los tres `application.yml`.
2. [ ] Migrar a Flyway con una línea base del esquema actual (trabajo futuro).
