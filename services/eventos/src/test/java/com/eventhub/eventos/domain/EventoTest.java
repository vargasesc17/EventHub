package com.eventhub.eventos.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EventoTest {

    private final Instant fechaFutura = Instant.now().plus(30, ChronoUnit.DAYS);
    private final UUID organizadorId = UUID.randomUUID();
    private final Instant ahora = Instant.now();

    private List<CategoriaBoleta> categoriasValidas() {
        return List.of(
                new CategoriaBoleta(TipoBoleta.GENERAL, 800, 50_000),
                new CategoriaBoleta(TipoBoleta.VIP, 200, 150_000)); // suman 1000
    }

    private Evento eventoEnBorrador() {
        return Evento.crear("Concierto", "Movistar Arena", fechaFutura, 1000,
                organizadorId, "org@correo.com", categoriasValidas(), ahora);
    }

    private Evento eventoEnEstado(EstadoEvento estado) {
        return Evento.reconstruir(UUID.randomUUID(), "Concierto", "Movistar Arena", fechaFutura, 1000,
                organizadorId, "org@correo.com", estado, categoriasValidas());
    }

    // ---------------------------------------------------------------- crear

    @Test
    void eventoNuevoQuedaEnBorrador() {
        assertEquals(EstadoEvento.BORRADOR, eventoEnBorrador().getEstado());
    }

    @Test
    void eventoNuevoTieneIdUnico() {
        var a = eventoEnBorrador();
        var b = eventoEnBorrador();

        assertNotNull(a.getId());
        assertNotEquals(a.getId(), b.getId());
    }

    @Test
    void rechazaSiLaSumaDeCuposSuperaElAforo() {
        var categorias = List.of(
                new CategoriaBoleta(TipoBoleta.GENERAL, 800, 50_000),
                new CategoriaBoleta(TipoBoleta.VIP, 300, 150_000)); // 1100 > 1000

        var error = assertThrows(ReglaDeNegocioException.class, () ->
                Evento.crear("Concierto", "Movistar Arena", fechaFutura, 1000,
                        organizadorId, "org@correo.com", categorias, ahora));

        assertTrue(error.getMessage().contains("1100"));
    }

    @Test
    void aceptaSiLaSumaDeCuposEsIgualAlAforo() {
        // categoriasValidas() suma exactamente 1000 = aforo
        assertDoesNotThrow(this::eventoEnBorrador);
    }

    @Test
    void rechazaEventoSinCategorias() {
        assertThrows(ReglaDeNegocioException.class, () ->
                Evento.crear("Concierto", "Movistar Arena", fechaFutura, 1000,
                        organizadorId, "org@correo.com", List.of(), ahora));
    }

    @Test
    void rechazaCategoriasNulas() {
        assertThrows(ReglaDeNegocioException.class, () ->
                Evento.crear("Concierto", "Movistar Arena", fechaFutura, 1000,
                        organizadorId, "org@correo.com", null, ahora));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void rechazaAforoQueNoSeaPositivo(int aforo) {
        assertThrows(ReglaDeNegocioException.class, () ->
                Evento.crear("Concierto", "Movistar Arena", fechaFutura, aforo,
                        organizadorId, "org@correo.com", categoriasValidas(), ahora));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void rechazaNombreVacio(String nombre) {
        assertThrows(ReglaDeNegocioException.class, () ->
                Evento.crear(nombre, "Movistar Arena", fechaFutura, 1000,
                        organizadorId, "org@correo.com", categoriasValidas(), ahora));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void rechazaLugarVacio(String lugar) {
        assertThrows(ReglaDeNegocioException.class, () ->
                Evento.crear("Concierto", lugar, fechaFutura, 1000,
                        organizadorId, "org@correo.com", categoriasValidas(), ahora));
    }

    @Test
    void rechazaFechaNula() {
        assertThrows(ReglaDeNegocioException.class, () ->
                Evento.crear("Concierto", "Movistar Arena", null, 1000,
                        organizadorId, "org@correo.com", categoriasValidas(), ahora));
    }

    @Test
    void rechazaCrearUnEventoEnElPasado() {
        var ayer = ahora.minus(1, ChronoUnit.DAYS);

        var error = assertThrows(ReglaDeNegocioException.class, () ->
                Evento.crear("Concierto", "Movistar Arena", ayer, 1000,
                        organizadorId, "org@correo.com", categoriasValidas(), ahora));

        assertTrue(error.getMessage().contains("futura"));
    }

    @Test
    void rechazaCrearUnEventoJustoAhora() {
        assertThrows(ReglaDeNegocioException.class, () ->
                Evento.crear("Concierto", "Movistar Arena", ahora, 1000,
                        organizadorId, "org@correo.com", categoriasValidas(), ahora));
    }

    @Test
    void rechazaEditarHaciaElPasado() {
        var evento = eventoEnBorrador();

        assertThrows(ReglaDeNegocioException.class, () ->
                evento.editar("Concierto", "Movistar Arena", ahora.minus(1, ChronoUnit.HOURS), 1000,
                        categoriasValidas(), ahora));
        assertEquals(fechaFutura, evento.getFechaInicio());   // no cambió nada
    }

    @Test
    void reconstruirAceptaEventosQueYaPasaron() {
        var hace30Dias = ahora.minus(30, ChronoUnit.DAYS);

        assertDoesNotThrow(() -> Evento.reconstruir(UUID.randomUUID(), "Concierto", "Movistar Arena",
                hace30Dias, 1000, organizadorId, "org@correo.com", EstadoEvento.FINALIZADO,
                categoriasValidas()));
    }

    // ---------------------------------------------------------- reconstruir

    @Test
    void reconstruirConservaTodosLosDatos() {
        var id = UUID.randomUUID();

        var evento = Evento.reconstruir(id, "Concierto", "Movistar Arena", fechaFutura, 1000,
                organizadorId, "org@correo.com", EstadoEvento.PUBLICADO, categoriasValidas());

        assertEquals(id, evento.getId());
        assertEquals("Concierto", evento.getNombre());
        assertEquals("Movistar Arena", evento.getLugar());
        assertEquals(fechaFutura, evento.getFechaInicio());
        assertEquals(1000, evento.getAforoMaximo());
        assertEquals(organizadorId, evento.getOrganizadorId());
        assertEquals("org@correo.com", evento.getOrganizadorCorreo());
        assertEquals(EstadoEvento.PUBLICADO, evento.getEstado());
        assertEquals(categoriasValidas(), evento.getCategorias());
    }

    // --------------------------------------------------------------- editar

    @Test
    void editarEnBorradorCambiaLosDatos() {
        var evento = eventoEnBorrador();
        var otraFecha = fechaFutura.plus(10, ChronoUnit.DAYS);
        var nuevas = List.of(new CategoriaBoleta(TipoBoleta.GENERAL, 1500, 80_000));

        evento.editar("Festival", "Parque Simón Bolívar", otraFecha, 2000, nuevas, ahora);

        assertEquals("Festival", evento.getNombre());
        assertEquals("Parque Simón Bolívar", evento.getLugar());
        assertEquals(otraFecha, evento.getFechaInicio());
        assertEquals(2000, evento.getAforoMaximo());
        assertEquals(nuevas, evento.getCategorias());
    }

    @ParameterizedTest
    @EnumSource(value = EstadoEvento.class, names = "BORRADOR", mode = EnumSource.Mode.EXCLUDE)
    void noSePuedeEditarFueraDeBorrador(EstadoEvento estado) {
        var evento = eventoEnEstado(estado);

        assertThrows(ReglaDeNegocioException.class, () ->
                evento.editar("Festival", "Parque Simón Bolívar", fechaFutura, 2000, categoriasValidas(), ahora));
        assertEquals("Concierto", evento.getNombre());
    }

    @Test
    void editarConDatosInvalidosNoModificaElEvento() {
        var evento = eventoEnBorrador();
        var excedidas = List.of(new CategoriaBoleta(TipoBoleta.GENERAL, 5000, 50_000));

        var error = assertThrows(ReglaDeNegocioException.class, () ->
                evento.editar("Festival", "Parque Simón Bolívar", fechaFutura, 1000, excedidas, ahora));

        // Debe fallar por los cupos, no por otra razón
        assertTrue(error.getMessage().contains("cupos"));
        assertEquals("Concierto", evento.getNombre());
        assertEquals("Movistar Arena", evento.getLugar());
        assertEquals(categoriasValidas(), evento.getCategorias());
    }

    // ----------------------------------------------------- copia defensiva

    @Test
    void lasCategoriasNoSePuedenModificarDesdeAfuera() {
        var evento = eventoEnBorrador();

        assertThrows(UnsupportedOperationException.class, () ->
                evento.getCategorias().add(new CategoriaBoleta(TipoBoleta.CORTESIA, 10, 0)));
    }

    @Test
    void crearCopiaLaListaRecibida() {
        var lista = new ArrayList<>(List.of(new CategoriaBoleta(TipoBoleta.GENERAL, 500, 50_000)));
        var evento = Evento.crear("Concierto", "Movistar Arena", fechaFutura, 1000,
                organizadorId, "org@correo.com", lista, ahora);

        lista.add(new CategoriaBoleta(TipoBoleta.VIP, 600, 150_000)); // ahora sumaría 1100

        assertEquals(1, evento.getCategorias().size());
    }

    @Test
    void editarNoGuardaUnaListaQueSePuedaModificar() {
        var evento = eventoEnBorrador();
        var lista = new ArrayList<>(List.of(new CategoriaBoleta(TipoBoleta.GENERAL, 500, 50_000)));

        evento.editar("Concierto", "Movistar Arena", fechaFutura, 1000, lista, ahora);
        lista.add(new CategoriaBoleta(TipoBoleta.VIP, 600, 150_000)); // ahora sumaría 1100

        assertEquals(1, evento.getCategorias().size());
    }
}