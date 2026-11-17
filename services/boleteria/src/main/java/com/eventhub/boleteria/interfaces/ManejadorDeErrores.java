package com.eventhub.boleteria.interfaces;

import com.eventhub.boleteria.application.EventoNoEncontradoException;
import com.eventhub.boleteria.application.PagoRechazadoException;
import com.eventhub.boleteria.application.SinCuposException;
import com.eventhub.boleteria.domain.ReglaDeNegocioException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
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

    @ExceptionHandler(SinCuposException.class)
    ProblemDetail sinCupos(SinCuposException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(PagoRechazadoException.class)
    ProblemDetail pagoRechazado(PagoRechazadoException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.PAYMENT_REQUIRED, e.getMessage());
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

    /** JSON mal formado o un valor que no existe, como "categoria": "PALCO". */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    ProblemDetail jsonInvalido(HttpMessageNotReadableException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "El cuerpo de la petición no es válido (revisa el formato y los valores de categoría)");
    }
}