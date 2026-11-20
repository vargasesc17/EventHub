# ADR-0010: Estrategia de pruebas

**Estado:** Aceptado
**Fecha:** 2026-11-12 (H2 y ArchUnit en el esqueleto, #51); 2026-11-17 (pruebas de concurrencia, #65)
**Documentado:** 2026-11-19
**Decisores:** Carlos Vargas, David Murcia, Karol Esquivel
**Responsable:** David Murcia
**Requerimientos que atiende:** RNF-01, RNF-02, RNF-03 (se demuestran con pruebas), todos los RF
**Issues relacionados:** #19–#24, #27

## Contexto

Los atributos de calidad más importantes del proyecto (no sobrevender, QR no falsificable, seguir vendiendo si Notificaciones se cae) no se pueden demostrar haciendo clic en la aplicación: hay que provocar compras simultáneas, QR alterados o mensajes fallidos. Además, las pruebas tenían que correr en el portátil de cualquier integrante sin levantar PostgreSQL ni RabbitMQ.

## Decisión

Cuatro tipos de prueba, todas con JUnit 5 y `mvn test` en cada servicio:

| Tipo | Qué prueba | Cómo | Ejemplos |
|---|---|---|---|
| Unitarias de dominio y casos de uso | Reglas de negocio | Fakes en memoria que implementan los puertos; sin Spring | `ComprarBoletaUseCaseTest`, `FabricaBoletaTest`, `PrecioStrategyTest`, `ValidacionQrHmacTest` |
| Integración | Controladores, JPA, mensajes | `@SpringBootTest` con **H2** en memoria | `BoletaControllerTest`, `RepositoriosJpaTest`, `JpaEventoRepositoryTest` |
| Arquitectura | Regla de dependencias | ArchUnit (ADR-0007) | `ArquitecturaTest` ×3 |
| Concurrencia y punta a punta | RNF-01 y RNF-03 | Hilos simultáneos sobre el mismo cupo o la misma boleta | `CompraConcurrenteTest`, `CheckInDePuntaAPuntaTest`, `ReintentosTest` |

## Opciones consideradas

### Opción A: fakes + H2 + ArchUnit + concurrencia ✅

| Dimensión | Evaluación |
|---|---|
| Complejidad | Media |
| Costo | Ninguno |
| Escalabilidad | Alta: las pruebas corren en segundos |
| Familiaridad del equipo | Media |

**Ventajas:** sin Docker para probar; cada RNF tiene una prueba que lo demuestra.
**Desventajas:** H2 no es PostgreSQL; un detalle de SQL específico podría pasar en H2 y fallar en producción.

### Opción B: Testcontainers (PostgreSQL y RabbitMQ reales en Docker)

| Dimensión | Evaluación |
|---|---|
| Complejidad | Media |
| Costo | Docker obligatorio y pruebas lentas |
| Escalabilidad | Alta |
| Familiaridad del equipo | Baja |

**Ventajas:** prueba contra la base real.
**Desventajas:** no todos los portátiles del equipo tenían Docker estable; descarga imágenes en cada máquina.

### Opción C: Mockito en lugar de fakes

**Desventajas:** las pruebas verifican llamadas en lugar de resultados y se rompen al refactorizar. Descartada para los casos de uso; los fakes son clases simples que cualquiera entiende.

### Opción D: solo pruebas manuales con Postman

**Desventajas:** no prueba concurrencia ni se repite en cada PR. Postman queda como evidencia para la sustentación, no como red de seguridad.

## Análisis del trade-off

Se acepta la diferencia H2/PostgreSQL a cambio de pruebas rápidas sin dependencias. El riesgo se cubre con el `docker compose up` completo y la colección de Postman antes de la entrega.

## Consecuencias

- **Más fácil:** cada PR se valida con `mvn test` en el servicio tocado.
- **Más fácil:** las pruebas de concurrencia son la evidencia directa de RNF-01 y RNF-03.
- **Más difícil:** mantener los fakes cuando cambia un puerto.
- **Riesgo:** no hay prueba de carga formal (RNF-04); queda como trabajo futuro.

## Acciones

1. [x] Fakes en memoria por puerto.
2. [x] `CompraConcurrenteTest` y `CheckInDePuntaAPuntaTest`.
3. [x] `ReintentosTest` en Boletería y Notificaciones (reintentos y DLQ).
4. [ ] Un solo comando para correr las pruebas de los tres servicios (#27).
