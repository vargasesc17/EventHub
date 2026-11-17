package com.eventhub.eventos.interfaces;

import com.eventhub.eventos.application.EventoNoEncontradoException;
import com.eventhub.eventos.domain.ReglaDeNegocioException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;

@RestControllerAdvice
public class ManejadorDeErrores {

    @ExceptionHandler(EventoNoEncontradoException.class)
    ProblemDetail noEncontrado(EventoNoEncontradoException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(ReglaDeNegocioException.class)
    ProblemDetail reglaDeNegocio(ReglaDeNegocioException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(422), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail datosInvalidos(MethodArgumentNotValidException e) {
        var problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Hay campos inválidos");
        var errores = new LinkedHashMap<String, String>();
        e.getBindingResult().getFieldErrors()
                .forEach(campo -> errores.put(campo.getField(), campo.getDefaultMessage()));
        problema.setProperty("errores", errores);
        return problema;
    }
}