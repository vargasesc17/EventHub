package com.eventhub.boleteria.interfaces;

import com.eventhub.boleteria.application.CheckInService;
import com.eventhub.boleteria.application.ResultadoCheckIn;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * #24 · El staff escanea el QR en la entrada. Responde 200 si la persona entra y 409 si se
 * rechaza (QR alterado, boleta ya usada, cancelada...), siempre con el motivo.
 * Depende de CheckInService, no de la compra (ISP).
 */
@RestController
public class CheckInController {

    private final CheckInService checkIn;
    private final TransactionTemplate transaccion;

    public CheckInController(CheckInService checkIn, TransactionTemplate transaccion) {
        this.checkIn = checkIn;
        this.transaccion = transaccion;
    }

    @PostMapping("/checkin")
    public ResponseEntity<ResultadoCheckIn> validar(@Valid @RequestBody CheckInRequest request) {
        var resultado = transaccion.execute(estado -> checkIn.validar(request.codigoQr(), request.staffId()));
        var codigo = resultado.aceptado() ? HttpStatus.OK : HttpStatus.CONFLICT;
        return ResponseEntity.status(codigo).body(resultado);
    }
}
