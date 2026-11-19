package com.eventhub.boleteria.interfaces;

import com.eventhub.boleteria.application.ComprarBoletaUseCase;
import com.eventhub.boleteria.application.ConsultarAforoUseCase;
import com.eventhub.boleteria.application.ConsultarBoletasDelCompradorUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "Boletas", description = "Compra de boletas, boletas del comprador y consulta de aforo")
public class BoletaController {

    private final ComprarBoletaUseCase comprar;
    private final ConsultarAforoUseCase consultarAforo;
    private final ConsultarBoletasDelCompradorUseCase consultarBoletas;
    private final TransactionTemplate transaccion;

    public BoletaController(ComprarBoletaUseCase comprar, ConsultarAforoUseCase consultarAforo,
                            ConsultarBoletasDelCompradorUseCase consultarBoletas, TransactionTemplate transaccion) {
        this.comprar = comprar;
        this.consultarAforo = consultarAforo;
        this.consultarBoletas = consultarBoletas;
        this.transaccion = transaccion;
    }

    /** Toda la compra en UNA transacción: si el pago falla, el cupo descontado vuelve. */
    @PostMapping("/boletas")
    @Operation(summary = "Comprar una boleta",
            description = """
                La fábrica del tipo (Factory Method) arma la boleta y la estrategia de la categoría (Strategy)
                calcula el precio. El cupo se descuenta con un UPDATE atómico, así que nunca se vende de más.
                ESTUDIANTE exige `carnetEstudiantil`; CORTESIA exige `emitidaPor` y no se cobra.
                Al terminar se publica `boleta.comprada` (correo con QR) y, si se cruza el 90 %, `evento.aforo.critico`.""")
    @ApiResponse(responseCode = "201", description = "Boleta comprada; incluye el código QR firmado")
    @ApiResponse(responseCode = "400", description = "Campos inválidos o JSON mal formado")
    @ApiResponse(responseCode = "402", description = "La pasarela rechazó el pago")
    @ApiResponse(responseCode = "404", description = "Boletería no conoce el evento")
    @ApiResponse(responseCode = "409", description = "No quedan cupos en esa categoría")
    @ApiResponse(responseCode = "422", description = "Evento no a la venta, categoría no vendida, falta carnet o emisor...")
    public ResponseEntity<CompraResponse> comprar(@Valid @RequestBody CompraRequest request) {
        var compra = transaccion.execute(estado -> comprar.comprar(request.aCompra()));
        return ResponseEntity.status(HttpStatus.CREATED).body(CompraResponse.desde(compra));
    }

    @GetMapping("/boletas")
    @Operation(summary = "Boletas de un comprador (\"Mis boletas\")",
            description = """
                De la compra más reciente a la más antigua, con el nombre, la fecha y el lugar del evento
                (copia local de Boletería, sin consultar a Eventos). El estado es el real: si el evento se
                canceló, la boleta llega CANCELADA. `codigoQr` solo viene en las boletas ACTIVA.
                Sin autenticación (sesión simulada), cualquiera que conozca el `compradorId` puede consultarlas.""")
    @ApiResponse(responseCode = "200", description = "Lista de boletas (vacía si no ha comprado)")
    @ApiResponse(responseCode = "400", description = "Falta el compradorId o no es un UUID")
    public List<BoletaDelCompradorResponse> boletasDelComprador(@RequestParam UUID compradorId) {
        return consultarBoletas.consultar(compradorId).stream().map(BoletaDelCompradorResponse::desde).toList();
    }

    @GetMapping("/eventos/{eventoId}/aforo")
    @Operation(summary = "Consultar el aforo de un evento",
            description = "Cupos, disponibles y vendidas por categoría, con su precio base y tipo de precio.")
    @ApiResponse(responseCode = "200", description = "Aforo por categoría")
    @ApiResponse(responseCode = "404", description = "Boletería no conoce el evento")
    public AforoResponse aforo(@PathVariable UUID eventoId) {
        return AforoResponse.desde(consultarAforo.consultar(eventoId));
    }
}