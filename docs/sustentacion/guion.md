# Guion de la sustentación de EventHub

**Orden:** primero la arquitectura (C4 niveles 1 y 2) y después cada ADR validado **funcionando**: en la aplicación, en Mailpit, en RabbitMQ, en el código o con Postman. Los RF y RNF se cubren a través de los ADR (tabla final).

**Reparto:**

| Integrante | Qué presenta |
|---|---|
| **Carlos** | C4, Eventos y la parte de Boletería de compra y aforo: ADR-0007, 0014, 0009, 0003, 0008 |
| **David** | La parte de Boletería del QR y check-in, y Notificaciones: ADR-0002, 0001, 0004, 0006, RNF-02 en vivo, 0010, 0013 |
| **Karol** | El frontend: ADR-0005, 0012, las pantallas de cada flujo y ADR-0011 (Git) |

En cada paso, **"Pantalla"** dice quién maneja el computador mientras otro habla. Duración estimada: 30 a 35 minutos.

---

## Antes de empezar (30 minutos antes)

Lo hace **David** en el computador de la demo:

1. Docker Desktop abierto. En la raíz del repositorio:
   ```bash
   docker compose down -v          # empezar con las bases vacías
   docker compose up -d --build
   docker compose ps               # todos en "healthy"
   ```
2. Abrir estas pestañas en el navegador, en este orden:

   | Pestaña | Dirección | Usuario |
   |---|---|---|
   | Aplicación | http://localhost:8080 | — |
   | Mailpit | http://localhost:8025 | — |
   | RabbitMQ | http://localhost:15672 | `RABBITMQ_USER` / `RABBITMQ_PASSWORD` del `.env` |
   | Swagger Eventos | http://localhost:8081/swagger-ui.html | — |
   | Swagger Boletería | http://localhost:8082/swagger-ui.html | — |
   | GitHub | el repositorio, pestaña Pull requests → Closed | — |

3. Postman: importar `docs/postman/EventHub.postman_collection.json` (está en la rama `docs/30-readme-y-coleccion` si aún no se ha fusionado). Correr la carpeta **0. Salud de los servicios**: las tres deben responder 200.
4. VS Code abierto en el repositorio, con estos archivos en pestañas:
   - `docs/diagramas/c1-contexto.svg` y `c2-contenedores.svg` (o el `docs/entrega-final.md` en vista previa)
   - `services/boleteria/src/test/java/com/eventhub/boleteria/ArquitecturaTest.java`
   - `services/boleteria/src/main/java/com/eventhub/boleteria/infrastructure/persistencia/InventarioCategoriaJpaRepository.java`
   - `services/boleteria/src/main/java/com/eventhub/boleteria/interfaces/ManejadorDeErrores.java`
   - `frontend/nginx.conf` y `frontend/src/sesion/SoloRol.jsx`
5. Una terminal en `services/boleteria` para correr pruebas.

**Plan B:** si Docker falla en vivo, se muestran las pruebas automáticas (`./mvnw test`) y Swagger; cada paso de abajo dice qué prueba lo respalda.

---

## Parte 1: Arquitectura (Carlos, 5 min)

### C1: Contexto
**Pantalla:** Carlos · **Dónde:** `c1-contexto.svg`

- EventHub es una sola caja. Tres personas lo usan: organizador, comprador y staff de check-in.
- Dos sistemas externos, **simulados** en esta entrega: la pasarela de pagos y el proveedor de correo (Mailpit).
- Las flechas están en lenguaje de negocio, sin protocolos: es el diagrama para alguien que no es técnico.

### C2: Contenedores
**Pantalla:** Carlos · **Dónde:** `c2-contenedores.svg`

Recorrerlo de arriba hacia abajo:

1. **Aplicación web** (React + Nginx): el único punto de entrada. → lo valida Karol en el ADR-0005.
2. **Tres microservicios**: Eventos (eventos, ciclo de vida y catálogo), **Boletería (dueña del aforo**, venta, QR y check-in), Notificaciones (correos).
3. **Una base por servicio**, incluida **BD Notificaciones**. → ADR-0002 y 0006 (David).
4. **RabbitMQ** con los tres eventos: `evento.actualizado`, `boleta.comprada`, `evento.aforo.critico`. → ADR-0001 (David).
5. **Mailpit** como proveedor de correo simulado.

Frase de cierre: *"Cada caja de este diagrama es un servicio del `docker-compose.yml`, y ahora vamos a mostrar que cada decisión que lo explica funciona."*

---

## Parte 2: Las decisiones, una por una

### 1. ADR-0007: Clean Architecture verificada con ArchUnit (Carlos, 3 min)
**Pantalla:** Carlos · **Dónde:** código + terminal

1. Mostrar los paquetes de Boletería: `domain`, `application`, `interfaces`, `infrastructure`.
2. Abrir `ArquitecturaTest.java`: *"el dominio no puede importar Spring ni JPA, y la aplicación no puede usar la infraestructura"*.
3. Correr solo esa prueba:
   ```bash
   ./mvnw -q test -Dtest=ArquitecturaTest
   ```
4. Contar el caso real: una prueba de #24 estaba en el paquete equivocado y **ArchUnit la detectó antes del merge**.

**Demuestra:** que la arquitectura no es solo un dibujo; el build la hace cumplir.

### 2. ADR-0005: Nginx como único punto de entrada (Karol, 3 min)
**Pantalla:** Karol · **Dónde:** aplicación (8080) + DevTools + `nginx.conf`

1. Abrir http://localhost:8080 y la pestaña **Network** de DevTools (F12).
2. Entrar al catálogo (`/eventos`): todas las peticiones van a `localhost:8080/api/eventos/...`, **nunca** a 8081 u 8082.
3. Mostrar `frontend/nginx.conf`: `/api/eventos` → `eventos:8080`, `/api/boleteria/` → `boleteria:8080`, cualquier otro `/api/` → 404.
4. *"Como todo sale del mismo origen, no hay CORS, y los servicios podrían no publicarse fuera de Docker."*

**Cubre:** RF-06 (catálogo), RNF-04.

### 3. ADR-0012: Sesión simulada por rol, sin JWT (Karol, 2 min)
**Pantalla:** Karol · **Dónde:** aplicación + `SoloRol.jsx`

1. Ir a `/login`: tres usuarios de prueba, uno por rol.
2. Entrar como **Karol (comprador)** e intentar abrir `/organizador`: la aplicación la devuelve al inicio.
3. Mostrar `SoloRol.jsx` (diez líneas).
4. Decirlo claro: *"No es seguridad real. Fue una decisión de alcance: el JWT era opcional (#32) y priorizamos los requisitos obligatorios. Está registrado como deuda."*

### 4. ADR-0014: Documentación de la API desde el código (Carlos, 2 min)
**Pantalla:** Carlos · **Dónde:** Swagger 8081 y 8082

1. Swagger de Eventos: `POST /eventos` con su esquema y sus respuestas de error.
2. Swagger de Boletería: `POST /boletas` y `POST /checkin`.
3. *"La documentación sale de los controladores; la que escribimos a mano (#66) se desactualizó en tres días."*

### 5. ADR-0009: Errores con ProblemDetail (Carlos, 3 min)
**Pantalla:** Carlos · **Dónde:** Postman, carpeta **1. Organizador**

1. **Crear evento en el pasado** → **422** con `title`, `status` y `detail`.
2. **Cortesía con precio** → **422**.
3. **Crear evento** → **201**, nace en `BORRADOR` (Postman guarda el `eventoId`).
4. Abrir `ManejadorDeErrores.java`: un solo lugar traduce excepciones a 400/404/409/422.
5. Karol, en la pantalla de crear evento (`/organizador/crear`), pone una fecha pasada: la aplicación muestra el mismo `detail`.

**Cubre:** RF-02 y sus reglas de negocio.

### 6. ADR-0002: Una base de datos por servicio (David, 2 min)
**Pantalla:** David · **Dónde:** terminal

1. Listar las bases:
   ```bash
   docker compose exec postgres psql -U <POSTGRES_ADMIN_USER> -d postgres -c "\l"
   ```
   Se ven `eventos_db`, `boleteria_db` y `notificaciones_db`, cada una con su dueño.
2. Intentar entrar a la base de Boletería con el usuario de Eventos:
   ```bash
   docker compose exec postgres psql -U <EVENTOS_DB_USER> -d boleteria_db
   ```
   Resultado esperado: **`permission denied for database boleteria_db`**.
3. *"La separación no depende de que nadie se equivoque: la impone PostgreSQL (`REVOKE ALL ... FROM PUBLIC` en el script de inicio)."*

(Reemplazar `<...>` por los valores del `.env`.)

### 7. ADR-0001: Comunicación asíncrona con RabbitMQ (David, 3 min)
**Pantalla:** David · **Dónde:** Postman + panel de RabbitMQ

1. RabbitMQ → **Exchanges**: `eventhub.events` (topic) y `eventhub.dlx`.
2. **Queues**: `boleteria.evento-actualizado`, `notificaciones.boleta-comprada`, `notificaciones.evento-actualizado`, `notificaciones.aforo-critico`, cada una con su `.dlq`.
3. En Postman: **Publicar evento**. En el panel se ve pasar el mensaje por `boleteria.evento-actualizado`.
4. Postman, carpeta **2. Comprador** → **Aforo inicial (Boletería)**: Boletería ya tiene el inventario (10 cupos) **sin haberle preguntado nada a Eventos por HTTP**; lo recibió por el mensaje.
5. Karol abre el catálogo en la aplicación: el evento ya aparece publicado.

**Cubre:** RF-02 (publicar), RF-06, patrón Observer.

### 8. ADR-0003: Boletería es dueña del aforo; compra sin sobreventa (Carlos, 5 min)
**Pantalla:** Carlos (Postman) y Karol (aplicación) · **Dónde:** Postman carpeta **2. Comprador**, aplicación, código

1. **Comprar GENERAL** y **Comprar VIP** → 201. *Factory Method: cada categoría crea su tipo de boleta. Strategy: el precio se calcula con la estrategia de la categoría.*
2. **ESTUDIANTE sin carné** → **422**. **Con carné** → 201.
3. **Emitir CORTESIA** → 201. **CORTESIA agotada** → **409** (la categoría tenía 1 cupo).
4. **Aforo después de las compras**: vendidas y disponibles por categoría.
5. Karol, como **comprador**, compra desde la aplicación (`/eventos/:id/comprar`) **3 GENERAL y 1 VIP**, una por una. Van 8 de 10 (GENERAL 4 de 5, VIP 2 de 3).
6. Carlos muestra la **prueba de concurrencia**:
   ```bash
   ./mvnw -q test -Dtest=CompraConcurrenteTest
   ```
   *100 hilos compran a la vez 10 cupos: exactamente 10 compras, 90 rechazos.*
7. *"Eventos define el aforo; Boletería lo controla, porque la verificación y el descuento deben ser una sola operación en una sola base."*

**Cubre:** RF-01, RF-05 (consulta), RNF-01.

### 9. ADR-0008: Spring Data JPA con UPDATE condicional (Carlos, 2 min)
**Pantalla:** Carlos · **Dónde:** `InventarioCategoriaJpaRepository.java`

1. Mostrar la consulta: `... where i.disponibles >= 1`.
2. *"Si afecta 0 filas, no había cupo: 409. No leemos y luego restamos, porque entre esas dos operaciones otra compra puede llegar."*
3. El mismo truco en el check-in: `cambiarEstadoSi(id, ACTIVA, USADA)` (lo muestra David en el siguiente paso).

### 10. ADR-0004: QR único y no falsificable (David, 4 min)
**Pantalla:** David (Mailpit, Postman) y Karol (pantalla del staff) · **Dónde:** Mailpit, aplicación, Postman carpeta **3. Staff**

1. **Mailpit**: abrir el correo de confirmación de una compra; trae el QR. El código es `boletaId.firma` (HMAC-SHA256 con `BOLETERIA_QR_SECRETO`, que no está en el repositorio).
2. Karol copia el código de una de las boletas que compró en la aplicación (detalle en `/boletas/:boletaId`), entra como **David (staff)**, abre `/checkin`, elige el evento y lo valida → **ingreso aceptado**.
3. Valida el mismo QR otra vez → **rechazado: ya fue usada**.
4. En Postman (usa el `codigoQr` de la boleta que se compró desde Postman, distinta de la que validó Karol):
   - **Check-in en la entrada de OTRO evento** → **409** y la boleta **sigue activa**.
   - **Check-in correcto** → 200.
   - **Check-in repetido** → 409.
   - **QR alterado** (una letra más) → 409.
5. *"Sin la clave no se puede fabricar la firma de otra boleta, y el paso de ACTIVA a USADA es atómico: dos puertas a la vez no dejan entrar a dos personas."*

**Cubre:** RF-03, RNF-03.

### 11. RNF-02 en vivo: Notificaciones caído (David, 4 min)
Valida juntas las decisiones de disponibilidad: **ADR-0001** (colas durables), **ADR-0002** y **ADR-0006**.
**Pantalla:** David · **Dónde:** terminal, Postman, RabbitMQ, Mailpit

1. Apagar Notificaciones:
   ```bash
   docker compose stop notificaciones
   ```
2. En Postman, **Comprar GENERAL** → **201**: la venta sigue funcionando. Esa compra es la 9 de 10, así que cruza el **90 % del aforo**. (Si en el ensayo la alerta no aparece, comprar también una VIP: la 10 de 10.)
3. RabbitMQ → Queues: `notificaciones.boleta-comprada` y `notificaciones.aforo-critico` tienen mensajes **en espera (Ready)**.
4. Encenderlo:
   ```bash
   docker compose start notificaciones
   ```
5. Las colas se vacían y en **Mailpit** llegan: la confirmación de la compra y la **alerta de aforo al organizador** (`organizador@eventhub.test`), una sola vez.
6. Mencionar los reintentos (1 s, 2 s, 4 s) y la **DLQ**: un mensaje que sigue fallando queda en `<cola>.dlq` para revisión, no se pierde.

**Cubre:** RNF-02, RF-04, RF-05 (alerta).

*Opcional, solo si se ensayó antes:* `docker compose stop mailpit`, hacer una compra y mostrar cómo el mensaje termina en `notificaciones.boleta-comprada.dlq` tras los reintentos; luego `docker compose start mailpit`.

### 12. ADR-0006: Notificaciones guarda su copia de compradores (David, 3 min)
**Pantalla:** David (Postman, Mailpit) y Karol (aplicación) · **Dónde:** Postman carpeta **4. Cambios del evento**

1. **Reprogramar: cambiar el lugar** → en Mailpit llega a cada comprador el aviso del cambio.
2. **Cancelar evento** → en Mailpit llega el aviso de cancelación a **todos** los compradores.
3. *"Notificaciones no le preguntó a Boletería quién compró: tiene su propia lista en `compradores_evento` (BD Notificaciones). Por eso puede avisar aunque Boletería esté ocupada o caída."*
4. Karol, como comprador, abre **Mis boletas** (`/boletas`): las boletas aparecen **canceladas**.
5. Postman: **Comprar en evento cancelado** → **422**.

**Cubre:** RF-04, RF-02 (cancelar), patrón Observer.

### 13. ADR-0010: Estrategia de pruebas (David, 2 min)
**Pantalla:** David · **Dónde:** terminal

```bash
cd services/boleteria && ./mvnw -q test
```

- Mostrar el resumen (todas en verde) y nombrar los cuatro tipos: unitarias con fakes en memoria, integración con H2, ArchUnit y concurrencia.
- *"Lo que no se puede mostrar haciendo clic (100 compras simultáneas, un QR con una letra cambiada) lo demuestran estas pruebas."*

### 14. ADR-0013: `ddl-auto: update` en lugar de Flyway (David, 1 min)
**Pantalla:** David · **Dónde:** `application.yml`

- Mostrar `ddl-auto: update`.
- *"Es deuda técnica consciente: con el modelo cambiando a diario, preferimos no mantener migraciones. Para producción pasaríamos a Flyway con una línea base del esquema actual."*

### 15. ADR-0011: GitHub Flow (Karol, 3 min)
**Pantalla:** Karol · **Dónde:** GitHub

1. Pestaña **Pull requests → Closed**: cada cambio entró por PR con revisión.
2. Abrir un PR con comentarios de revisión (por ejemplo #62 o #68).
3. Mostrar `CONTRIBUTING.md`: ramas `tipo/N-descripcion`, Conventional Commits, una aprobación, squash.
4. Contar el caso real: su rama del frontend partía de una versión vieja de `main` y se rescató como **#73** con `Co-authored-by`. *"Aprendimos a actualizar la rama desde `main` todos los días."*
5. Mostrar la tabla de participación del documento final (sección 13).

---

## Cierre (Carlos, 1 min)

- Lo que quedó fuera, dicho por nosotros antes de que lo pregunten: reservas de 24 h, autenticación real, prueba de carga formal, outbox.
- Todo está en `docs/entrega-final.md`, secciones 14 (lecciones) y 15 (limitaciones).

---

## Cobertura: dónde se valida cada requerimiento

| Requerimiento | Paso del guion | Dónde se ve |
|---|---|---|
| RF-01 Compra por categoría | 8 | Postman + aplicación |
| RF-02 Eventos y ciclo de vida | 5, 7, 12 | Postman + aplicación |
| RF-03 Check-in con QR | 10 | Aplicación (staff) + Postman |
| RF-04 Correos a compradores | 10, 11, 12 | Mailpit |
| RF-05 Aforo y alerta al 90 % | 8, 11 | Postman + Mailpit |
| RF-06 Catálogo | 2, 7 | Aplicación |
| RNF-01 No sobrevender | 8, 9 | Postman (409) + `CompraConcurrenteTest` |
| RNF-02 Disponibilidad | 11 | RabbitMQ + Mailpit |
| RNF-03 QR seguro | 10 | Aplicación + Postman |
| RNF-04 Rendimiento | 2, 9 | DevTools + código (sin prueba de carga) |

## Resumen por integrante

| Integrante | Pasos | Tiempo aprox. |
|---|---|---|
| Carlos | C1, C2, 1, 4, 5, 8, 9, cierre | 15 min |
| David | Preparación, 6, 7, 10, 11, 12, 13, 14 | 14 min |
| Karol | 2, 3, 15 + pantalla en 5, 7, 8, 10, 12 | 8 min + apoyo |

## Preguntas probables y quién responde

| Pregunta | Responde |
|---|---|
| ¿Por qué el aforo no está en Eventos si el enunciado lo dice? | Carlos (ADR-0003) |
| ¿Qué pasa si RabbitMQ se cae? | David (Eventos registra el error; sin outbox el mensaje se pierde: trabajo futuro) |
| ¿Y si alguien copia el QR de la foto de otro? | David (entra una sola vez: el segundo es rechazado) |
| ¿Por qué no JWT? | Karol (ADR-0012) |
| ¿Por qué GitHub Flow y no Gitflow? | Karol (ADR-0011) |
| ¿Por qué las tres bases están en un solo PostgreSQL? | David (ahorro de recursos; un usuario por base sin permisos cruzados) |
| ¿Qué cambiarían si empezaran de nuevo? | Cualquiera (sección 14.3) |
