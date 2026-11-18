# Arquitectura por roles

- [x] Carpetas y vistas JSX agrupadas por rol.
- [x] Rutas protegidas con sesión simulada e inicio según rol.
- [x] Navegación compartida por rol.
- [x] Eventos: catálogo, detalle, creación, edición de borrador, reprogramación y transiciones por POST.
- [x] Identificadores de eventos y respuestas de compras en localStorage, separados por UUID de usuario.
- [x] Detalle de boleta con QR mediante qrcode.react.
- [x] Proxy de Vite y Nginx para rutas relativas.
- [ ] Habilitar compra y aforo después de integrar PR #65 y verificar los DTO y las rutas internas de Boletería.
- [ ] Conectar Check-in cuando se complete #24 y exista listado de eventos del staff.
- [ ] Conectar notificaciones cuando exista un endpoint de consulta.

## Uso local

Desde frontend: `npm install` y `npm run dev`. El proxy utiliza los servicios locales de Docker: Eventos en 8081 y Boletería en 8082. Las páginas solicitan exclusivamente rutas relativas `/api/...`.

El proxy de Eventos retira `/api`. El proxy reservado para Boletería retira `/api/boleteria`; confirmar este contrato interno cuando se integre PR #65 antes de habilitar las pantallas.

Selecciona un usuario fijo de prueba en `/login`. La sesión simulada no proporciona autorización real; el backend deberá aplicar la seguridad cuando se implemente autenticación.

Los eventos propios se recuperan consultando los identificadores guardados en este navegador. No se inventa un endpoint de listado. Borrar los datos del navegador elimina este historial local. Lo mismo aplica a las boletas. `guardarMiBoleta(usuario.id, respuesta)` queda disponible para guardar la respuesta 201 cuando se habilite la compra; no se generan identificadores de comprador.

Las fechas del formulario se interpretan en la zona horaria del navegador y se envían con `toISOString()` en UTC. Los precios son enteros en COP. Eventos usa `tipo`; el contrato de Aforo usará `categoria`.

## Validación

Se ejecutaron `npm run build`, `npm run lint` y comprobaciones con mocks de aislamiento por usuario, deduplicación, conservación de almacenamiento dañado, cancelación POST, reprogramación PATCH y errores ProblemDetail/respuestas HTML.

La integración real con servicios y la navegación visual requieren verificación con el entorno ejecutándose.
