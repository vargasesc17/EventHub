package com.eventhub.boleteria.interfaces;

import com.eventhub.boleteria.application.ComprarBoletaUseCase;
import com.eventhub.boleteria.application.ConsultarAforoUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class BoletaController {

    private final ComprarBoletaUseCase comprar;
    private final ConsultarAforoUseCase consultarAforo;
    private final TransactionTemplate transaccion;

    public BoletaController(ComprarBoletaUseCase comprar, ConsultarAforoUseCase consultarAforo,
                            TransactionTemplate transaccion) {
        this.comprar = comprar;
        this.consultarAforo = consultarAforo;
        this.transaccion = transaccion;
    }

    /** Toda la compra en UNA transacción: si el pago falla, el cupo descontado vuelve. */
    @PostMapping("/boletas")
    public ResponseEntity<CompraResponse> comprar(@Valid @RequestBody CompraRequest request) {
        var compra = transaccion.execute(estado -> comprar.comprar(request.aCompra()));
        return ResponseEntity.status(HttpStatus.CREATED).body(CompraResponse.desde(compra));
    }

    @GetMapping("/eventos/{eventoId}/aforo")
    public AforoResponse aforo(@PathVariable UUID eventoId) {
        return AforoResponse.desde(consultarAforo.consultar(eventoId));
    }
}