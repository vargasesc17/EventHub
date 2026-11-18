# Frontend de EventHub

Aplicación web para los tres roles del sistema: comprador, organizador y staff.

## Stack

| Tecnología | Para qué |
|---|---|
| **React 19 + Vite** | Interfaz por componentes y compilación rápida |
| **React Router** | Rutas por rol y protección de pantallas |
| **Tailwind CSS** | Estilos sin hojas CSS por pantalla |
| **qrcode.react** | Dibujar el QR de la boleta a partir del texto que devuelve la API |
| **Nginx** | Sirve la aplicación y es el punto de entrada a la API ([ADR-0005](../docs/adr/0005-acceso-frontend.md)) |

El frontend solo usa rutas relativas (`/api/eventos/...`, `/api/boleteria/...`). Nginx las reenvía a cada microservicio, así que la página y la API comparten origen y no hace falta CORS. Los endpoints están en [`docs/api`](../docs/api/README.md).

## Cómo correrlo

**Con Docker (como en la demo)**, desde la raíz del repo:

```bash
cp .env.example .env      # solo la primera vez
docker compose up -d --build
```

Abrir http://localhost:8080.

**Para desarrollar** (recarga automática al guardar), con el backend ya levantado en Docker:

```bash
cd frontend
npm install
npm run dev
```

Abrir http://localhost:5173. Vite reenvía `/api/eventos` al puerto 8081 y `/api/boleteria` al 8082.

## Usuarios de prueba

En `/login` se elige uno:

| Usuario | Rol | Qué puede hacer |
|---|---|---|
| Carlos Vargas | Organizador | Crear, editar, publicar, reprogramar y cancelar eventos; ver el aforo |
| Karol Esquivel | Comprador | Ver el catálogo, comprar boletas y ver sus QR |
| David Murcia | Staff | Validar boletas en la entrada (check-in) |

> **La sesión es simulada.** Sirve para mostrar cada rol, pero no da seguridad: ocultar una pantalla no impide llamar a la API directamente. La autenticación real (JWT con roles) es la bonificación #32 y queda fuera de esta entrega.

## Limitaciones

- **"Mis boletas" y "Mis eventos" se guardan en el navegador**, porque la API no tiene endpoints para listarlos. Si se borran los datos del navegador, se pierde ese historial (las boletas y eventos siguen existiendo en el backend).
- **Notificaciones no tiene pantalla de bandeja:** los avisos llegan por correo (en desarrollo, a Mailpit en http://localhost:8025).
- **El staff solo ve eventos publicados con fecha futura**, porque la lista sale del catálogo.

## Estructura

```
src/
├── api/          Llamadas a la API (eventos, boletería) y manejo de errores
├── components/   Piezas reutilizables (Navbar, EventCard, FormularioEvento…)
├── pages/
│   ├── comprador/
│   ├── organizador/
│   ├── staff/
│   └── comunes/  Login
├── sesion/       Sesión simulada y protección de rutas por rol
└── storage/      "Mis boletas" y "Mis eventos" en el navegador
```