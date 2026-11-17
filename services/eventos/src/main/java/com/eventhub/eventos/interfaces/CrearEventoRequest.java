package com.eventhub.eventos.interfaces;

import com.eventhub.eventos.application.CrearEventoCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CrearEventoRequest(@NotBlank String nombre,
                                 @NotBlank String lugar,
                                 @NotNull Instant fechaInicio,
                                 @Positive int aforoMaximo,
                                 @NotNull UUID organizadorId,
                                 @NotBlank @Email String organizadorCorreo,
                                 @NotEmpty List<@Valid CategoriaRequest> categorias) {

    CrearEventoCommand aCommand() {
        return new CrearEventoCommand(nombre, lugar, fechaInicio, aforoMaximo, organizadorId,
                organizadorCorreo, categorias.stream().map(CategoriaRequest::aDominio).toList());
    }
}