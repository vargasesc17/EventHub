package com.eventhub.eventos.interfaces;

import com.eventhub.eventos.application.EditarEventoCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Instant;
import java.util.List;

public record EditarEventoRequest(@NotBlank String nombre,
                                  @NotBlank String lugar,
                                  @NotNull Instant fechaInicio,
                                  @Positive int aforoMaximo,
                                  @NotEmpty List<@Valid CategoriaRequest> categorias) {

    EditarEventoCommand aCommand() {
        return new EditarEventoCommand(nombre, lugar, fechaInicio, aforoMaximo,
                categorias.stream().map(CategoriaRequest::aDominio).toList());
    }
}