# ADR-0008: Spring Boot y Spring Data JPA, con UPDATE condicionales para los cambios críticos

**Estado:** Aceptado
**Fecha:** 2026-11-12 (stack, #51); 2026-11-17 (UPDATE condicionales, #65 y #68)
**Documentado:** 2026-11-19
**Decisores:** Carlos Vargas, David Murcia, Karol Esquivel
**Responsable:** Carlos Vargas
**Requerimientos que atiende:** RNF-01 (consistencia), RNF-03 (QR de uso único), RNF-04 (rendimiento), RF-01, RF-03
**Issues relacionados:** #16–#18, #23, #24

## Contexto

Había que elegir el framework de los tres microservicios y la forma de acceder a PostgreSQL. Dos operaciones tienen condiciones de carrera reales:

- **Comprar la última boleta** (RNF-01): dos compras simultáneas no pueden descontar el mismo cupo (ADR-0003).
- **Usar una boleta en el check-in** (RNF-03): dos lectores del mismo QR no pueden dejar entrar a dos personas (ADR-0004).

El resto (crear eventos, listar el catálogo, consultar boletas) es CRUD sin competencia por la misma fila.

## Decisión

- **Spring Boot 4.1.2 sobre Java 25** en los tres servicios, con Spring Web, Spring AMQP y Spring Data JPA.
- **Spring Data JPA** para el CRUD normal (repositorios `*JpaRepository` dentro de `infrastructure`, detrás de los puertos del dominio, ADR-0007).
- **Las dos operaciones críticas no usan "leer y luego guardar"**, sino un `UPDATE` condicional con `@Modifying` + `@Query`, que la base ejecuta de forma atómica:

```java
update InventarioCategoriaEntity i
set i.disponibles = i.disponibles - 1, i.vendidas = i.vendidas + 1
where i.eventoId = :eventoId and i.categoria = :categoria and i.disponibles >= 1
```

```java
// check-in: solo pasa de ACTIVA a USADA si sigue ACTIVA
cambiarEstadoSi(id, EstadoBoleta.ACTIVA, EstadoBoleta.USADA)
```

Si la consulta afecta 0 filas, la operación se rechaza (409). Los estados se pasan como parámetros, no como literales JPQL.

## Opciones consideradas

### Opción A: Spring Boot + JPA, con UPDATE condicional en lo crítico ✅

| Dimensión | Evaluación |
|---|---|
| Complejidad | Baja |
| Costo | Ninguno |
| Escalabilidad | Alta: el bloqueo dura un `UPDATE` |
| Familiaridad del equipo | Alta |

**Ventajas:** JPA ahorra el código repetitivo del CRUD; las dos reglas críticas quedan en una sola instrucción que la base garantiza.
**Desventajas:** hay que acordarse de `flushAutomatically`/`clearAutomatically` para que la entidad en memoria no quede desactualizada.

### Opción B: JPA puro con bloqueo optimista (`@Version`)

| Dimensión | Evaluación |
|---|---|
| Complejidad | Media |
| Costo | Ninguno |
| Escalabilidad | Baja en picos: muchos reintentos |
| Familiaridad del equipo | Media |

**Desventajas:** en una venta muy demandada casi todas las compras fallan y reintentan.

### Opción C: JDBC/`JdbcTemplate` en todo el servicio

| Dimensión | Evaluación |
|---|---|
| Complejidad | Media |
| Costo | Ninguno |
| Escalabilidad | Alta |
| Familiaridad del equipo | Media |

**Desventajas:** SQL a mano para todo el CRUD; más código sin beneficio fuera de las dos operaciones críticas.

### Opción D: otro framework (Quarkus, Micronaut, Node)

**Desventajas:** el equipo conoce Spring; cambiar de stack no aporta a los requerimientos. Descartada.

## Análisis del trade-off

Se usa JPA donde no hay competencia y SQL condicional donde sí la hay. Es el punto intermedio entre la comodidad de JPA y la garantía de atomicidad que necesitan RNF-01 y RNF-03.

## Consecuencias

- **Más fácil:** `CompraConcurrenteTest` lanza N hilos sobre el último cupo y solo uno compra.
- **Más fácil:** el check-in repetido o simultáneo devuelve 409 sin código de bloqueo propio.
- **Más difícil:** Java 25 y Spring Boot 4 son muy recientes: Jackson 3 cambia el paquete a `tools.jackson.*` y hay poca documentación.
- **Riesgo:** si una regla de venta se vuelve compleja (por ejemplo, combos de varias categorías), el `UPDATE` condicional deja de alcanzar y habría que pasar a bloqueo pesimista.

## Acciones

1. [x] Compra con `UPDATE` condicional y alerta del 90 % una sola vez (#65).
2. [x] Check-in con `cambiarEstadoSi` (#68).
3. [x] Pruebas de concurrencia (`CompraConcurrenteTest`).
