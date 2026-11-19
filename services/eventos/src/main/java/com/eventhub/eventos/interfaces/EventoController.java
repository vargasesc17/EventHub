package com.eventhub.eventos.interfaces;

import com.eventhub.eventos.application.ConsultarEventosUseCase;
import com.eventhub.eventos.application.GestionarEventoUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/eventos")
@Tag(name = "Eventos", description = "Creación, edición y ciclo de vida de los eventos")
public class EventoController {

    private final GestionarEventoUseCase gestionar;
    private final ConsultarEventosUseCase consultar;

    public EventoController(GestionarEventoUseCase gestionar, ConsultarEventosUseCase consultar) {
        this.gestionar = gestionar;
        this.consultar = consultar;
    }

    @Operation(summary = "Crear un evento",
            description = "El evento nace en BORRADOR. La fecha de inicio debe ser futura y debe tener al menos "
                    + "una categoría de boleta; la cortesía tiene precio 0.")
    @ApiResponse(responseCode = "201", description = "Evento creado en BORRADOR")
    @ApiResponse(responseCode = "400", description = "Campos obligatorios vacíos o inválidos")
    @ApiResponse(responseCode = "422", description = "Regla de negocio violada (fecha en el pasado, cupos o precio inválidos, suma de cupos mayor al aforo, cortesía con precio...)")
    @PostMapping
    public ResponseEntity<EventoResponse> crear(@Valid @RequestBody CrearEventoRequest request) {
        var evento = gestionar.crear(request.aCommand());
        return ResponseEntity.created(URI.create("/eventos/" + evento.getId()))
                .body(EventoResponse.desde(evento));
    }

    @Operation(summary = "Consultar un evento por id")
    @ApiResponse(responseCode = "200", description = "El evento")
    @ApiResponse(responseCode = "404", description = "No existe un evento con ese id")
    @GetMapping("/{id}")
    public EventoResponse consultar(@Parameter(description = "Id del evento") @PathVariable UUID id) {
        return EventoResponse.desde(consultar.buscarPorId(id));
    }

    @Operation(summary = "Editar un evento",
            description = "Solo se puede editar mientras está en BORRADOR, y la fecha debe seguir siendo futura.")
    @ApiResponse(responseCode = "200", description = "Evento editado")
    @ApiResponse(responseCode = "400", description = "Campos inválidos")
    @ApiResponse(responseCode = "404", description = "No existe el evento")
    @ApiResponse(responseCode = "422", description = "El evento ya no está en BORRADOR, o la nueva fecha está en el pasado")
    @PutMapping("/{id}")
    public EventoResponse editar(@PathVariable UUID id, @Valid @RequestBody EditarEventoRequest request) {
        return EventoResponse.desde(gestionar.editar(id, request.aCommand()));
    }

    @Operation(summary = "Publicar un evento",
            description = "BORRADOR → PUBLICADO. Publica `evento.actualizado`: Boletería crea el inventario "
                    + "y empieza a vender.")
    @ApiResponse(responseCode = "200", description = "Evento publicado")
    @ApiResponse(responseCode = "404", description = "No existe el evento")
    @ApiResponse(responseCode = "422", description = "Transición no permitida desde el estado actual")
    @PostMapping("/{id}/publicar")
    public EventoResponse publicar(@PathVariable UUID id) {
        return EventoResponse.desde(gestionar.publicar(id));
    }

    @Operation(summary = "Iniciar un evento", description = "PUBLICADO → EN_CURSO.")
    @ApiResponse(responseCode = "200", description = "Evento en curso")
    @ApiResponse(responseCode = "404", description = "No existe el evento")
    @ApiResponse(responseCode = "422", description = "Transición no permitida desde el estado actual")
    @PostMapping("/{id}/iniciar")
    public EventoResponse iniciar(@PathVariable UUID id) {
        return EventoResponse.desde(gestionar.iniciar(id));
    }

    @Operation(summary = "Finalizar un evento", description = "EN_CURSO → FINALIZADO.")
    @ApiResponse(responseCode = "200", description = "Evento finalizado")
    @ApiResponse(responseCode = "404", description = "No existe el evento")
    @ApiResponse(responseCode = "422", description = "Transición no permitida desde el estado actual")
    @PostMapping("/{id}/finalizar")
    public EventoResponse finalizar(@PathVariable UUID id) {
        return EventoResponse.desde(gestionar.finalizar(id));
    }

    @Operation(summary = "Cancelar un evento",
            description = "BORRADOR o PUBLICADO → CANCELADO. Si estaba publicado, publica `evento.actualizado`: "
                    + "Boletería bloquea las ventas y cancela las boletas vendidas; Notificaciones avisa "
                    + "a los compradores. Un borrador se cancela sin publicar nada.")
    @ApiResponse(responseCode = "200", description = "Evento cancelado")
    @ApiResponse(responseCode = "404", description = "No existe el evento")
    @ApiResponse(responseCode = "422", description = "El evento está EN_CURSO, FINALIZADO o ya CANCELADO")
    @PostMapping("/{id}/cancelar")
    public EventoResponse cancelar(@PathVariable UUID id) {
        return EventoResponse.desde(gestionar.cancelar(id));
    }

    @Operation(summary = "Reprogramar fecha o lugar",
            description = "Solo para eventos PUBLICADOS. `fechaInicio` y `lugar` son opcionales; si ninguno cambia, "
                    + "responde 200 sin guardar ni avisar. Si cambia algo, publica UN `evento.actualizado` con "
                    + "todos los cambios y los valores anteriores, y Notificaciones avisa a los compradores.")
    @ApiResponse(responseCode = "200", description = "Evento reprogramado (o sin cambios)")
    @ApiResponse(responseCode = "404", description = "No existe el evento")
    @ApiResponse(responseCode = "422", description = "El evento no está PUBLICADO, la nueva fecha está en el pasado o el lugar viene vacío")
    @PatchMapping("/{id}")
    public EventoResponse reprogramar(@PathVariable UUID id, @RequestBody ReprogramarEventoRequest request) {
        return EventoResponse.desde(gestionar.reprogramar(id, request.aCommand()));
    }

    @Operation(summary = "Catálogo",
            description = "Eventos PUBLICADOS cuya fecha de inicio todavía no llega (RF-06). "
                    + "Los que están en curso, finalizados o cancelados no aparecen.")
    @ApiResponse(responseCode = "200", description = "Lista de eventos")
    @GetMapping("/catalogo")
    public List<EventoResponse> catalogo() {
        return consultar.catalogo().stream().map(EventoResponse::desde).toList();
    }
}