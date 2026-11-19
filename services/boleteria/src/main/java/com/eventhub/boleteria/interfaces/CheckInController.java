package com.eventhub.boleteria.interfaces;

import com.eventhub.boleteria.application.CheckInService;
import com.eventhub.boleteria.application.ResultadoCheckIn;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Check-in", description = "Validación del QR en la entrada (staff)")
public class CheckInController {

    private final CheckInService checkIn;
    private final TransactionTemplate transaccion;

    public CheckInController(CheckInService checkIn, TransactionTemplate transaccion) {
        this.checkIn = checkIn;
        this.transaccion = transaccion;
    }

    @PostMapping("/checkin")
    @Operation(summary = "Validar el QR de una boleta",
            description = """
                El código tiene la forma `boletaId.firma` (HMAC-SHA256, ADR-0004). Si la firma es válida, la
                boleta es del evento indicado en `eventoId` y está ACTIVA, se marca USADA de forma atómica:
                el mismo QR no entra dos veces. Siempre responde con el motivo.""")
    @ApiResponse(responseCode = "200", description = "Aceptado: la persona entra")
    @ApiResponse(responseCode = "409", description = "Rechazado: QR alterado, boleta de otro evento, ya usada o cancelada")
    @ApiResponse(responseCode = "400", description = "Falta el código, el eventoId o el staffId")
    public ResponseEntity<ResultadoCheckIn> validar(@Valid @RequestBody CheckInRequest request) {
        var resultado = transaccion.execute(estado -> checkIn.validar(request.codigoQr(), request.eventoId(), request.staffId()));
        var codigo = resultado.aceptado() ? HttpStatus.OK : HttpStatus.CONFLICT;
        return ResponseEntity.status(codigo).body(resultado);
    }
}
