# Diagramas de EventHub

| Archivo | Qué muestra |
|---|---|
| `c1-contexto.svg` | C4 nivel 1: el sistema y sus usuarios |
| `c2-contenedores.svg` | C4 nivel 2: microservicios, bases de datos y broker |
| `clases-eventos` (`.puml`, `.svg`, `.png`) | Clases del microservicio Eventos (#14) |
| `clases-boleteria` (`.puml`, `.svg`, `.png`) | Clases de Boletería: jerarquía de boletas, Factory Method, Strategy, ISP y DIP (#14) |
| `clases-notificaciones` (`.puml`, `.svg`, `.png`) | Clases de Notificaciones, igual a como está implementado (#14) |

## Diagrama de clases (#14)

Cubre lo que pide el issue:

- **Entidades:** `Evento`, `CategoriaBoleta`, `Boleta`, `Comprador`, `CheckIn`, con sus atributos.
- **Jerarquía de boletas:** `BoletaGeneral`, `BoletaVIP`, `BoletaEstudiante`, `BoletaCortesia`, y su fábrica (**Factory Method**: `FabricaBoleta` y una subclase por tipo).
- **Strategy:** la interfaz `PrecioStrategy` con `PrecioFijo`, `PrecioEarlyBird` y `PrecioDinamico`.
- **A qué microservicio pertenece cada clase:** un diagrama por servicio.
- **ISP:** `CompraBoletaService` y `CheckInService` son interfaces separadas.
- **DIP:** `ComprarBoletaUseCase` depende de `PrecioStrategy` y de puertos (interfaces), no de clases concretas.
- **Clean Architecture:** cada servicio tiene los paquetes `domain`, `application`, `infrastructure` e `interfaces` (la misma estructura del código de `services/`). Los controladores REST y los consumidores de RabbitMQ están en `interfaces`, porque son la entrada desde afuera.

Las clases de Eventos y Boletería son el **diseño propuesto** a partir de los ADRs y los issues #19–#24; si la implementación cambia, se actualiza el `.puml`. Las de Notificaciones ya están implementadas.

## Cómo editarlos

Los `.puml` son texto en [PlantUML](https://plantuml.com/es/class-diagram). Para verlos y exportarlos desde VS Code, instala la extensión **PlantUML** (de jebbs), abre el `.puml` y presiona `Alt + D`. Para regenerar la imagen: clic derecho → *Export Current Diagram* → SVG o PNG.
