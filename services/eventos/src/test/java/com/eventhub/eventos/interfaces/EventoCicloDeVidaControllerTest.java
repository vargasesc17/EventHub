package com.eventhub.eventos.interfaces;

import com.eventhub.eventos.application.EventoRepository;
import com.eventhub.eventos.application.PublicadorEventosEnMemoria;
import com.eventhub.eventos.domain.CategoriaBoleta;
import com.eventhub.eventos.domain.EstadoEvento;
import com.eventhub.eventos.domain.Evento;
import com.eventhub.eventos.domain.TipoBoleta;
import com.eventhub.eventos.domain.TipoCambio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class EventoCicloDeVidaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventoRepository repositorio;

    @Autowired
    private PublicadorEventosEnMemoria publicador;

    @BeforeEach
    void limpiarPublicaciones() {
        publicador.limpiar();
    }

    private String guardarEnEstado(EstadoEvento estado) {
        var evento = Evento.reconstruir(UUID.randomUUID(), "Concierto", "Movistar Arena",
                Instant.parse("2030-06-01T20:00:00Z"), 1000, UUID.randomUUID(), "org@correo.com", estado,
                List.of(new CategoriaBoleta(TipoBoleta.GENERAL, 1000, 50_000)));
        repositorio.guardar(evento);
        return "/eventos/" + evento.getId();
    }

    @Test
    void publicarDevuelveElEventoPublicadoYAvisa() throws Exception {
        var url = guardarEnEstado(EstadoEvento.BORRADOR);

        mockMvc.perform(post(url + "/publicar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PUBLICADO"));

        assertEquals(1, publicador.publicaciones().size());
        assertEquals(Set.of(TipoCambio.PUBLICADO), publicador.publicaciones().get(0).cambios());
    }

    @Test
    void unEventoPublicadoApareceEnElCatalogo() throws Exception {
        var url = guardarEnEstado(EstadoEvento.BORRADOR);
        mockMvc.perform(get("/eventos/catalogo")).andExpect(jsonPath("$", hasSize(0)));

        mockMvc.perform(post(url + "/publicar")).andExpect(status().isOk());

        mockMvc.perform(get("/eventos/catalogo"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombre").value("Concierto"));
    }

    @Test
    void cicloCompletoPorLaApi() throws Exception {
        var url = guardarEnEstado(EstadoEvento.BORRADOR);

        mockMvc.perform(post(url + "/publicar")).andExpect(jsonPath("$.estado").value("PUBLICADO"));
        mockMvc.perform(post(url + "/iniciar")).andExpect(jsonPath("$.estado").value("EN_CURSO"));
        mockMvc.perform(post(url + "/finalizar")).andExpect(jsonPath("$.estado").value("FINALIZADO"));

        mockMvc.perform(get(url)).andExpect(jsonPath("$.estado").value("FINALIZADO"));
    }

    @Test
    void transicionInvalidaDevuelve422ConUnMensajeClaroYNoAvisa() throws Exception {
        var url = guardarEnEstado(EstadoEvento.BORRADOR);

        mockMvc.perform(post(url + "/finalizar"))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.detail", containsString("BORRADOR")))
                .andExpect(jsonPath("$.detail", containsString("FINALIZADO")));

        mockMvc.perform(get(url)).andExpect(jsonPath("$.estado").value("BORRADOR"));
        assertTrue(publicador.publicaciones().isEmpty());
    }

    @Test
    void cancelarUnPublicadoLoSacaDelCatalogoYAvisa() throws Exception {
        var url = guardarEnEstado(EstadoEvento.PUBLICADO);

        mockMvc.perform(post(url + "/cancelar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADO"));

        mockMvc.perform(get("/eventos/catalogo")).andExpect(jsonPath("$", hasSize(0)));
        assertEquals(Set.of(TipoCambio.CANCELADO), publicador.publicaciones().get(0).cambios());
    }

    @Test
    void reprogramarCambiaFechaYLugarYAvisaUnaSolaVez() throws Exception {
        var url = guardarEnEstado(EstadoEvento.PUBLICADO);

        mockMvc.perform(patch(url).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fechaInicio": "2030-06-03T20:00:00Z", "lugar": "Coliseo, Chía"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lugar").value("Coliseo, Chía"));

        assertEquals(1, publicador.publicaciones().size());
        var publicacion = publicador.publicaciones().get(0);
        assertEquals(Set.of(TipoCambio.FECHA, TipoCambio.LUGAR), publicacion.cambios());
        assertEquals("Movistar Arena", publicacion.anteriores().lugar());
    }

    @Test
    void reprogramarSoloElLugarDejaLaFechaIgual() throws Exception {
        var url = guardarEnEstado(EstadoEvento.PUBLICADO);

        mockMvc.perform(patch(url).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"lugar": "Coliseo, Chía"}
                                """))
                .andExpect(status().isOk());

        assertEquals(Set.of(TipoCambio.LUGAR), publicador.publicaciones().get(0).cambios());
    }

    @Test
    void reprogramarUnBorradorDevuelve422() throws Exception {
        var url = guardarEnEstado(EstadoEvento.BORRADOR);

        mockMvc.perform(patch(url).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"lugar": "Coliseo, Chía"}
                                """))
                .andExpect(status().is(422));

        assertTrue(publicador.publicaciones().isEmpty());
    }

    @Test
    void operarSobreUnEventoInexistenteDevuelve404() throws Exception {
        mockMvc.perform(post("/eventos/" + UUID.randomUUID() + "/publicar"))
                .andExpect(status().isNotFound());
    }
}