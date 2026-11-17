package com.eventhub.eventos.interfaces;

import com.eventhub.eventos.application.EventoRepository;
import com.eventhub.eventos.domain.CategoriaBoleta;
import com.eventhub.eventos.domain.EstadoEvento;
import com.eventhub.eventos.domain.Evento;
import com.eventhub.eventos.domain.TipoBoleta;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class EventoControllerTest {

    private static final String EVENTO_VALIDO = """
            {
              "nombre": "Concierto",
              "lugar": "Movistar Arena",
              "fechaInicio": "2030-06-01T20:00:00Z",
              "aforoMaximo": 1000,
              "organizadorId": "3f2b8c1e-5a4d-4e6f-9b7a-1c2d3e4f5a6b",
              "organizadorCorreo": "org@correo.com",
              "categorias": [
                {"tipo": "GENERAL", "cupos": 800, "precioBase": 50000},
                {"tipo": "VIP", "cupos": 200, "precioBase": 150000}
              ]
            }
            """;

    private static final String EDICION_VALIDA = """
            {
              "nombre": "Festival",
              "lugar": "Parque Simón Bolívar",
              "fechaInicio": "2030-07-01T20:00:00Z",
              "aforoMaximo": 500,
              "categorias": [{"tipo": "ESTUDIANTE", "cupos": 500, "precioBase": 30000}]
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventoRepository repositorio;

    /** Crea un evento por la API y devuelve su URL (/eventos/{id}), tomada del encabezado Location. */
    private String crearEvento() throws Exception {
        var location = mockMvc.perform(post("/eventos").contentType(MediaType.APPLICATION_JSON).content(EVENTO_VALIDO))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");
        assertNotNull(location);
        return location;
    }

    private Evento guardarEnEstado(String nombre, EstadoEvento estado) {
        var evento = Evento.reconstruir(UUID.randomUUID(), nombre, "Movistar Arena",
                Instant.now().plus(30, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS), 1000,
                UUID.randomUUID(), "org@correo.com", estado,
                List.of(new CategoriaBoleta(TipoBoleta.GENERAL, 1000, 50_000)));
        repositorio.guardar(evento);
        return evento;
    }

    @Test
    void crearDevuelve201ConElEventoEnBorrador() throws Exception {
        mockMvc.perform(post("/eventos").contentType(MediaType.APPLICATION_JSON).content(EVENTO_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.estado").value("BORRADOR"))
                .andExpect(jsonPath("$.nombre").value("Concierto"))
                .andExpect(jsonPath("$.categorias", hasSize(2)));
    }

    @Test
    void crearConCamposInvalidosDevuelve400ConElCampo() throws Exception {
        var sinNombre = EVENTO_VALIDO.replace("\"Concierto\"", "\"\"");

        mockMvc.perform(post("/eventos").contentType(MediaType.APPLICATION_JSON).content(sinNombre))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.nombre").exists());
    }

    @Test
    void crearConCuposQueSuperanElAforoDevuelve422() throws Exception {
        var aforoPequeno = EVENTO_VALIDO.replace("\"aforoMaximo\": 1000", "\"aforoMaximo\": 500");

        mockMvc.perform(post("/eventos").contentType(MediaType.APPLICATION_JSON).content(aforoPequeno))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.detail").exists());
    }

    @Test
    void consultarDevuelveElEventoCreado() throws Exception {
        var url = crearEvento();

        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Concierto"))
                .andExpect(jsonPath("$.lugar").value("Movistar Arena"))
                .andExpect(jsonPath("$.aforoMaximo").value(1000))
                .andExpect(jsonPath("$.categorias[0].tipo").value("GENERAL"));
    }

    @Test
    void consultarEventoInexistenteDevuelve404() throws Exception {
        mockMvc.perform(get("/eventos/" + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void editarEnBorradorDevuelveElEventoActualizado() throws Exception {
        var url = crearEvento();

        mockMvc.perform(put(url).contentType(MediaType.APPLICATION_JSON).content(EDICION_VALIDA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Festival"))
                .andExpect(jsonPath("$.aforoMaximo").value(500))
                .andExpect(jsonPath("$.categorias", hasSize(1)));
    }

    @Test
    void editarUnEventoPublicadoDevuelve422() throws Exception {
        var publicado = guardarEnEstado("Publicado", EstadoEvento.PUBLICADO);

        mockMvc.perform(put("/eventos/" + publicado.getId())
                        .contentType(MediaType.APPLICATION_JSON).content(EDICION_VALIDA))
                .andExpect(status().is(422));
    }

    @Test
    void catalogoSoloMuestraEventosPublicados() throws Exception {
        crearEvento(); // queda en BORRADOR: no debe aparecer
        guardarEnEstado("Visible", EstadoEvento.PUBLICADO);
        guardarEnEstado("Cancelado", EstadoEvento.CANCELADO);

        mockMvc.perform(get("/eventos/catalogo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombre").value("Visible"));
    }
}