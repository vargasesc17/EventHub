package com.eventhub.eventos.interfaces;

import com.eventhub.eventos.application.ConsultarEventosUseCase;
import com.eventhub.eventos.application.GestionarEventoUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PatchMapping;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/eventos")
public class EventoController {

    private final GestionarEventoUseCase gestionar;
    private final ConsultarEventosUseCase consultar;

    public EventoController(GestionarEventoUseCase gestionar, ConsultarEventosUseCase consultar) {
        this.gestionar = gestionar;
        this.consultar = consultar;
    }

    @PostMapping
    public ResponseEntity<EventoResponse> crear(@Valid @RequestBody CrearEventoRequest request) {
        var evento = gestionar.crear(request.aCommand());
        return ResponseEntity.created(URI.create("/eventos/" + evento.getId()))
                .body(EventoResponse.desde(evento));
    }

    @GetMapping("/{id}")
    public EventoResponse consultar(@PathVariable UUID id) {
        return EventoResponse.desde(consultar.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public EventoResponse editar(@PathVariable UUID id, @Valid @RequestBody EditarEventoRequest request) {
        return EventoResponse.desde(gestionar.editar(id, request.aCommand()));
    }

    @PostMapping("/{id}/publicar")
    public EventoResponse publicar(@PathVariable UUID id) {
        return EventoResponse.desde(gestionar.publicar(id));
    }

    @PostMapping("/{id}/iniciar")
    public EventoResponse iniciar(@PathVariable UUID id) {
        return EventoResponse.desde(gestionar.iniciar(id));
    }

    @PostMapping("/{id}/finalizar")
    public EventoResponse finalizar(@PathVariable UUID id) {
        return EventoResponse.desde(gestionar.finalizar(id));
    }

    @PostMapping("/{id}/cancelar")
    public EventoResponse cancelar(@PathVariable UUID id) {
        return EventoResponse.desde(gestionar.cancelar(id));
    }

    @PatchMapping("/{id}")
    public EventoResponse reprogramar(@PathVariable UUID id, @RequestBody ReprogramarEventoRequest request) {
        return EventoResponse.desde(gestionar.reprogramar(id, request.aCommand()));
    }

    @GetMapping("/catalogo")
    public List<EventoResponse> catalogo() {
        return consultar.catalogo().stream().map(EventoResponse::desde).toList();
    }
}