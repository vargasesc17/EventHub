# ADR-0003: Control de concurrencia en la compra y responsable del aforo

**Estado:** Aceptado
**Fecha:** 2026-11-09
**Decisores:** Carlos Vargas, David Murcia, Karol Esquivel
**Responsable:** Carlos Vargas
**Requerimientos que atiende:** RNF-01 (Consistencia), RF-01 (Compra y reserva), RF-05 (Alerta de aforo), RNF-04 (Rendimiento)
**Issues relacionados:** #13, #19, #20, #23

## Contexto

El RNF-01 exige que **nunca se vendan más boletas que el aforo disponible**, incluso si varias personas compran la última boleta al mismo tiempo. Es la condición de carrera clásica: dos compras leen "queda 1 cupo", ambas lo descuentan y se vende una boleta de más.

Hay además una tensión en el enunciado:

- El principio SRP del documento dice que **la validación del aforo pertenece al servicio de Eventos**.
- Pero quien vende las boletas y sabe cuántas quedan es **Boletería**.

Si cada compra tuviera que preguntarle a Eventos por HTTP si hay cupo, Boletería dependería de Eventos para vender (afecta RNF-02) y la verificación no sería atómica (dos compras podrían recibir "sí hay cupo" a la vez).

## Decisión

Se separan dos responsabilidades que el enunciado agrupa bajo "aforo":

1. **Eventos es dueño de la definición del aforo.** Define el aforo máximo y valida al configurar el evento que la suma de cupos de las categorías no lo supere.
2. **Boletería es dueña del inventario y de la alerta.** Recibe los cupos por categoría al publicarse el evento, controla el descuento de cada compra y, como es quien sabe cuántas boletas se han vendido, **publica `evento.aforo.critico`** cuando las vendidas alcanzan o superan el 90 % del aforo.

El control de concurrencia se hace con una **actualización condicional atómica** en PostgreSQL:

```sql
UPDATE inventario_categoria
SET disponibles = disponibles - :cantidad
WHERE categoria_id = :id
  AND disponibles >= :cantidad;
```

Si la consulta afecta **0 filas**, no había cupo y la compra se rechaza. La base de datos garantiza que dos actualizaciones sobre la misma fila no se intercalen, así que nunca se descuenta por debajo de cero.

La alerta se publica **una sola vez** por evento. Para eso, en la misma transacción de la compra se marca la alerta como enviada con otra actualización condicional:

```sql
UPDATE aforo_evento
SET alerta_enviada = true
WHERE evento_id = :eventoId
  AND alerta_enviada = false
  AND vendidas * 100 >= aforo_maximo * 90;
```

Solo la compra que logra cambiar la fila (1 fila afectada) publica el mensaje. Si dos compras cruzan el 90 % al mismo tiempo, solo una lo consigue.

Las **reservas** (RF-01) descuentan inventario de la misma forma. Una tarea programada devuelve al inventario las reservas que superen 24 horas sin pago.

## Opciones consideradas

### Opción A: leer el cupo y luego descontar en la aplicación

| Dimensión | Evaluación |
|---|---|
| Complejidad | Baja |
| Costo | Ninguno |
| Escalabilidad | Alta |
| Familiaridad del equipo | Alta |

**Ventajas:** el código más simple de escribir.
**Desventajas:** **tiene la condición de carrera.** Incumple RNF-01. Descartada.

### Opción B: bloqueo pesimista (`SELECT ... FOR UPDATE`)

| Dimensión | Evaluación |
|---|---|
| Complejidad | Media |
| Costo | Ninguno |
| Escalabilidad | Media: las compras de una misma categoría esperan en fila |
| Familiaridad del equipo | Media |

**Ventajas:** correcto; permite lógica compleja entre la lectura y la escritura.
**Desventajas:** mantiene la fila bloqueada durante la transacción; riesgo de bloqueos mutuos si se toca más de una categoría.

### Opción C: bloqueo optimista (columna `version`, `@Version` en JPA)

| Dimensión | Evaluación |
|---|---|
| Complejidad | Media |
| Costo | Ninguno |
| Escalabilidad | Alta con poca contención; baja en picos |
| Familiaridad del equipo | Media |

**Ventajas:** no bloquea; muy usado en Spring.
**Desventajas:** en un pico de compras sobre la misma categoría, la mayoría de intentos falla y tiene que reintentar.

### Opción D: actualización condicional atómica ✅

| Dimensión | Evaluación |
|---|---|
| Complejidad | Baja |
| Costo | Ninguno |
| Escalabilidad | Alta: el bloqueo dura solo lo que tarda el `UPDATE` |
| Familiaridad del equipo | Alta: es SQL estándar |

**Ventajas:** correcta, simple y rápida; una sola instrucción.
**Desventajas:** sirve porque la regla es simple ("hay cupo o no"); reglas más complejas exigirían la opción B.

### Opción E: Boletería consulta el aforo a Eventos por HTTP en cada compra

**Desventajas:** no es atómica entre servicios y hace que la compra dependa de Eventos. Descartada.

### Quién publica `evento.aforo.critico`

| | Eventos lo publica | Boletería lo publica ✅ |
|---|---|---|
| Cómo | Consume `boleta.comprada`, lleva su propio conteo y publica al cruzar el 90 % | Publica al confirmar la compra que cruza el 90 % |
| Flujos | Uno más (Eventos consume y publica) | Ninguno adicional |
| Precisión | Eventual: el conteo de Eventos va unos segundos atrasado | Exacta: el conteo sale del mismo inventario que se acaba de descontar |
| SRP del enunciado | Lo sigue al pie de la letra | Se aparta: Boletería asume la vigilancia del umbral |
| Duplicación | El conteo de vendidas existe en dos servicios | El conteo existe en un solo lugar |

## Análisis del trade-off

La consistencia (RNF-01) se resuelve **dentro de un solo servicio y una sola base**, porque garantizar atomicidad entre servicios distintos exigiría transacciones distribuidas, mucho más complejas. Por eso el inventario vive en Boletería, aunque el enunciado asocie el aforo con Eventos.

Por la misma razón, la alerta la publica Boletería: el dato que dispara la alerta (las boletas vendidas) vive ahí, y publicarla desde Eventos obligaría a mantener un segundo conteo en otro servicio. Se elige la opción **más simple y más precisa**, aceptando apartarse del ejemplo de SRP del enunciado. El SRP se defiende así: Eventos responde a cambios en **cómo se define un evento**; Boletería responde a cambios en **cómo se vende**, y la alerta del 90 % es una regla de venta.

Entre las opciones correctas (B, C y D), la D da el mejor rendimiento en picos (RNF-04) con el código más simple.

## Consecuencias

- **Más fácil:** el test de concurrencia es directo: N hilos compran la última boleta y solo uno lo logra.
- **Más fácil:** Boletería vende aunque Eventos esté caído.
- **Más fácil:** la alerta es exacta y no requiere un flujo adicional entre servicios.
- **Más difícil:** Boletería necesita conocer el aforo máximo de cada evento, que recibe de Eventos al publicarse el evento.
- **Riesgo:** si el umbral cambia (por ejemplo, del 90 % al 80 %), el cambio se hace en Boletería, aunque el enunciado asocie el aforo con Eventos. Conviene que el umbral sea configurable.
- **Más difícil:** el aforo queda representado en dos lugares (definición en Eventos, inventario en Boletería). Si un organizador cambia el aforo de un evento publicado, Eventos debe publicar `evento.actualizado` y Boletería ajustar su inventario.
- **Revisar:** la duración de la reserva (24 h). En eventos muy demandados bloquea inventario; las plataformas comerciales usan entre 10 y 15 minutos.

## Acciones

1. [ ] Implementar la compra con `UPDATE` condicional (#23).
2. [ ] Test de concurrencia: N compras simultáneas sobre los últimos cupos (#23, #27).
3. [ ] Publicar `evento.aforo.critico` una sola vez al cruzar el 90 % (#23).
4. [ ] Test: dos compras que cruzan el 90 % al mismo tiempo publican una sola alerta (#23, #27).
5. [ ] Implementar la tarea que libera reservas vencidas (#23).
6. [ ] Quitar del diagrama C2 la flecha "consume boleta.comprada" de Eventos (#35).
