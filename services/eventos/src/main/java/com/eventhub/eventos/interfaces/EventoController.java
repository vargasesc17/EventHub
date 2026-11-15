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

    @GetMapping("/catalogo")
    public List<EventoResponse> catalogo() {
        return consultar.catalogo().stream().map(EventoResponse::desde).toList();
    }
}