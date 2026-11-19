package com.eventhub.boleteria.application;

import com.eventhub.boleteria.domain.Boleta;
import com.eventhub.boleteria.domain.CheckIn;

import java.time.Clock;
import java.util.UUID;

/**
 * #24 · Check-in con el QR (RF-03, RNF-03, ADR-0004). Validación en dos pasos:
 * <ol>
 *   <li><b>Integridad:</b> se revisa la firma del QR. Si fue alterado o inventado, se rechaza SIN
 *       tocar la base de datos.</li>
 *   <li><b>Uso único:</b> se marca la boleta como USADA con una actualización condicional atómica.
 *       Un segundo escaneo (o dos al mismo tiempo) se rechaza.</li>
 * </ol>
 * Además, la boleta debe ser del evento en cuya entrada está el staff: un QR auténtico de otro
 * evento se rechaza y no se marca como usada.
 * El flujo trabaja con la clase abstracta Boleta: es idéntico para VIP, General, Estudiante y
 * Cortesía (LSP). Debe ejecutarse dentro de una transacción.
 */
public class CheckInUseCase implements CheckInService {

    private final ValidadorQr validadorQr;
    private final BoletaRepository boletas;
    private final CheckInRepository checkIns;
    private final Clock reloj;

    public CheckInUseCase(ValidadorQr validadorQr, BoletaRepository boletas, CheckInRepository checkIns,
                          Clock reloj) {
        this.validadorQr = validadorQr;
        this.boletas = boletas;
        this.checkIns = checkIns;
        this.reloj = reloj;
    }

    @Override
    public ResultadoCheckIn validar(String codigoQr, UUID eventoId, UUID staffId) {
        if (staffId == null) {
            throw new IllegalArgumentException("El check-in exige el id del staff que valida");
        }
        if (eventoId == null) {
            throw new IllegalArgumentException("El check-in exige el evento en el que se está validando");
        }

        // Paso 1: integridad (RNF-03). Un QR falso no llega a la base.
        var boletaId = validadorQr.boletaIdSiEsAutentico(codigoQr).orElse(null);
        if (boletaId == null) {
            return ResultadoCheckIn.rechazado("QR inválido: fue alterado o no lo emitió EventHub", null);
        }

        Boleta boleta = boletas.buscar(boletaId).orElse(null);
        if (boleta == null) {
            return ResultadoCheckIn.rechazado("La boleta no existe", boletaId);
        }
        // Antes que el estado: una boleta de otro evento no entra, y su estado no es asunto de esta puerta.
        if (!boleta.getEventoId().equals(eventoId)) {
            return ResultadoCheckIn.rechazado("La boleta es de otro evento", boletaId);
        }
        if (!boleta.puedeIngresar()) {
            return ResultadoCheckIn.rechazado(motivoSegunEstado(boleta), boletaId);
        }

        // Paso 2: uso único (RF-03). Si otro staff la usó un instante antes, aquí se detecta.
        if (!boletas.marcarUsadaSiActiva(boletaId)) {
            return ResultadoCheckIn.rechazado("La boleta ya fue usada", boletaId);
        }
        checkIns.registrar(new CheckIn(UUID.randomUUID(), boletaId, boleta.getEventoId(), staffId, reloj.instant()));
        return ResultadoCheckIn.aceptado(boletaId, boleta.categoria());
    }

    private static String motivoSegunEstado(Boleta boleta) {
        return switch (boleta.getEstado()) {
            case USADA -> "La boleta ya fue usada";
            case CANCELADA -> "La boleta está cancelada";
            case RESERVADA -> "La boleta está reservada y todavía no se ha pagado";
            case ACTIVA -> "La boleta no puede ingresar";
        };
    }
}
