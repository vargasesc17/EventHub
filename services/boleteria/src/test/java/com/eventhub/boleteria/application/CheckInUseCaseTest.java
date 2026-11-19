package com.eventhub.boleteria.application;

import com.eventhub.boleteria.domain.Boleta;
import com.eventhub.boleteria.domain.BoletaCortesia;
import com.eventhub.boleteria.domain.BoletaEstudiante;
import com.eventhub.boleteria.domain.BoletaGeneral;
import com.eventhub.boleteria.domain.BoletaVIP;
import com.eventhub.boleteria.domain.CheckIn;
import com.eventhub.boleteria.domain.EstadoBoleta;
import com.eventhub.boleteria.domain.TipoBoleta;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** #24 · Check-in con el QR: dos pasos (firma y uso único) y el mismo flujo para todo tipo de boleta. */
class CheckInUseCaseTest {

    /** QR falso para los tests: "id.ok" es auténtico; cualquier otra cosa es un QR alterado. */
    private final ValidadorQr validador = codigo -> {
        if (codigo == null || !codigo.endsWith(".ok")) {
            return Optional.empty();
        }
        return Optional.of(UUID.fromString(codigo.substring(0, codigo.length() - 3)));
    };
    private final BoletasEnMemoria boletas = new BoletasEnMemoria();
    private final List<CheckIn> checkIns = new ArrayList<>();
    private final Clock reloj = Clock.fixed(Instant.parse("2030-06-01T19:45:00Z"), ZoneOffset.UTC);
    private final CheckInUseCase checkIn = new CheckInUseCase(validador, boletas, checkIns::add, reloj);

    private final UUID eventoId = UUID.randomUUID();
    private final UUID staffId = UUID.randomUUID();
    private final Instant compra = Instant.parse("2030-05-01T12:00:00Z");

    private BoletaGeneral general(EstadoBoleta estado) {
        return new BoletaGeneral(UUID.randomUUID(), eventoId, UUID.randomUUID(), 80_000, estado, compra);
    }

    private static String qr(Boleta boleta) {
        return boleta.getId() + ".ok";
    }

    @Test
    void unaBoletaActivaEntraYQuedaRegistradoQuienYCuando() {
        var boleta = general(EstadoBoleta.ACTIVA);
        boletas.guardar(boleta);

        var resultado = checkIn.validar(qr(boleta), eventoId, staffId);

        assertTrue(resultado.aceptado());
        assertEquals(TipoBoleta.GENERAL, resultado.categoria());
        assertEquals(1, checkIns.size());
        var registro = checkIns.get(0);
        assertEquals(boleta.getId(), registro.boletaId());
        assertEquals(eventoId, registro.eventoId());
        assertEquals(staffId, registro.staffId());
        assertEquals(reloj.instant(), registro.fecha());
    }

    @Test
    void laMismaBoletaNoEntraDosVeces() {
        var boleta = general(EstadoBoleta.ACTIVA);
        boletas.guardar(boleta);

        checkIn.validar(qr(boleta), eventoId, staffId);
        var segunda = checkIn.validar(qr(boleta), eventoId, UUID.randomUUID());

        assertFalse(segunda.aceptado());
        assertEquals("La boleta ya fue usada", segunda.motivo());
        assertEquals(1, checkIns.size());
    }

    @Test
    void unQrAlteradoSeRechazaSinConsultarLaBase() {
        var boleta = general(EstadoBoleta.ACTIVA);
        boletas.guardar(boleta);

        var resultado = checkIn.validar(boleta.getId() + ".firma-cambiada", eventoId, staffId);

        assertFalse(resultado.aceptado());
        assertTrue(resultado.motivo().startsWith("QR inválido"));
        assertEquals(0, boletas.consultas(), "Un QR falso no debe llegar a la base (ADR-0004)");
        assertTrue(checkIns.isEmpty());
    }

    @Test
    void unaBoletaQueNoExisteSeRechaza() {
        var resultado = checkIn.validar(UUID.randomUUID() + ".ok", eventoId, staffId);

        assertFalse(resultado.aceptado());
        assertEquals("La boleta no existe", resultado.motivo());
    }

    @Test
    void unaBoletaCanceladaOReservadaNoEntra() {
        var cancelada = general(EstadoBoleta.CANCELADA);
        var reservada = general(EstadoBoleta.RESERVADA);
        boletas.guardar(cancelada);
        boletas.guardar(reservada);

        assertEquals("La boleta está cancelada", checkIn.validar(qr(cancelada), eventoId, staffId).motivo());
        assertFalse(checkIn.validar(qr(reservada), eventoId, staffId).aceptado());
        assertTrue(checkIns.isEmpty());
    }

    @Test
    void elFlujoEsElMismoParaLosCuatroTiposDeBoleta() {
        // LSP: el check-in solo conoce la clase abstracta Boleta. Cualquier subtipo se comporta igual.
        List<Boleta> todas = List.of(
                general(EstadoBoleta.ACTIVA),
                new BoletaVIP(UUID.randomUUID(), eventoId, UUID.randomUUID(), 250_000, EstadoBoleta.ACTIVA, compra,
                        List.of("Zona preferencial")),
                new BoletaEstudiante(UUID.randomUUID(), eventoId, UUID.randomUUID(), 50_000, EstadoBoleta.ACTIVA,
                        compra, "U2024-1234"),
                new BoletaCortesia(UUID.randomUUID(), eventoId, UUID.randomUUID(), 0, EstadoBoleta.ACTIVA, compra,
                        UUID.randomUUID()));
        todas.forEach(boletas::guardar);

        for (Boleta boleta : todas) {
            assertTrue(checkIn.validar(qr(boleta), eventoId, staffId).aceptado(), "Primer ingreso de " + boleta.categoria());
            assertFalse(checkIn.validar(qr(boleta), eventoId, staffId).aceptado(), "Segundo ingreso de " + boleta.categoria());
        }
        assertEquals(4, checkIns.size());
    }

    @Test
    void sinStaffNoSePuedeHacerCheckIn() {
        assertThrows(IllegalArgumentException.class, () -> checkIn.validar(UUID.randomUUID() + ".ok", eventoId, null));
    }

    @Test
    void unaBoletaDeOtroEventoNoEntraYNoSeGasta() {
        var deOtroEvento = new BoletaGeneral(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), 80_000,
                EstadoBoleta.ACTIVA, compra);
        boletas.guardar(deOtroEvento);

        var resultado = checkIn.validar(qr(deOtroEvento), eventoId, staffId);

        assertFalse(resultado.aceptado());
        assertEquals("La boleta es de otro evento", resultado.motivo());
        assertTrue(checkIns.isEmpty());
        // No se marcó como usada: en la entrada de SU evento todavía puede entrar.
        assertTrue(checkIn.validar(qr(deOtroEvento), deOtroEvento.getEventoId(), staffId).aceptado());
    }

    @Test
    void sinEventoNoSePuedeHacerCheckIn() {
        assertThrows(IllegalArgumentException.class, () -> checkIn.validar(UUID.randomUUID() + ".ok", null, staffId));
    }
}
