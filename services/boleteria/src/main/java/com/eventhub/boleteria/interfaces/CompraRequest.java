package com.eventhub.boleteria.interfaces;

import com.eventhub.boleteria.domain.Compra;
import com.eventhub.boleteria.domain.TipoBoleta;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/** carnetEstudiantil solo aplica a ESTUDIANTE; emitidaPor solo a CORTESIA. */
public record CompraRequest(@NotNull UUID eventoId,
                            @NotNull TipoBoleta categoria,
                            @NotNull UUID compradorId,
                            @NotBlank String nombreComprador,
                            @NotBlank @Email String correoComprador,
                            String carnetEstudiantil,
                            UUID emitidaPor) {

    Compra aCompra() {
        return new Compra(eventoId, categoria, compradorId, nombreComprador, correoComprador,
                carnetEstudiantil, emitidaPor);
    }
}