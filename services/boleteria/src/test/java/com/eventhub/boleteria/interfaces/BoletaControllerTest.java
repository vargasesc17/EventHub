package com.eventhub.boleteria.interfaces;

import com.eventhub.boleteria.application.AforoRepository;
import com.eventhub.boleteria.application.InventarioRepository;
import com.eventhub.boleteria.domain.AforoEvento;
import com.eventhub.boleteria.domain.InventarioCategoria;
import com.eventhub.boleteria.domain.TipoBoleta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class BoletaControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private AforoRepository aforos;
    @Autowired private InventarioRepository inventarios;

    private final UUID eventoId = UUID.randomUUID();

    @BeforeEach
    void eventoALaVenta() {
        aforos.crear(AforoEvento.nuevo(eventoId, "Concierto", Instant.parse("2030-06-01T20:00:00Z"),
                "Movistar Arena", UUID.randomUUID(), "org@correo.com", 100));
        inventarios.crear(List.of(
                InventarioCategoria.nuevo(eventoId, TipoBoleta.GENERAL, 99, 50_000),
                InventarioCategoria.nuevo(eventoId, TipoBoleta.VIP, 1, 150_000)));
    }

    private ResultActions comprar(UUID evento, String categoria, String correo) throws Exception {
        var cuerpo = """
                {"eventoId": "%s", "categoria": "%s", "compradorId": "%s",
                 "nombreComprador": "Ana Pérez", "correoComprador": "%s"}
                """.formatted(evento, categoria, UUID.randomUUID(), correo);
        return mockMvc.perform(post("/boletas").contentType(MediaType.APPLICATION_JSON).content(cuerpo));
    }

    @Test
    void comprarDevuelve201ConLaBoletaYSuQr() throws Exception {
        comprar(eventoId, "VIP", "ana@correo.com")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.categoria").value("VIP"))
                .andExpect(jsonPath("$.precioPagado").value(150000))
                .andExpect(jsonPath("$.estado").value("ACTIVA"))
                .andExpect(jsonPath("$.codigoQr").value(org.hamcrest.Matchers.matchesPattern(
                "[0-9a-f-]{36}\\.[A-Za-z0-9_-]{43}")));
    }

    @Test
    void sinCuposDevuelve409() throws Exception {
        comprar(eventoId, "VIP", "ana@correo.com").andExpect(status().isCreated());

        comprar(eventoId, "VIP", "ana@correo.com").andExpect(status().isConflict());
    }

    @Test
    void unEventoDesconocidoDevuelve404() throws Exception {
        comprar(UUID.randomUUID(), "GENERAL", "ana@correo.com").andExpect(status().isNotFound());
    }

    @Test
    void unCorreoInvalidoDevuelve400ConElCampo() throws Exception {
        comprar(eventoId, "GENERAL", "no-es-correo")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.correoComprador").exists());
    }

    @Test
    void unaCategoriaQueNoExisteDevuelve400() throws Exception {
        comprar(eventoId, "PALCO", "ana@correo.com").andExpect(status().isBadRequest());
    }

    @Test
    void unEventoCanceladoDevuelve422() throws Exception {
        aforos.bloquearVentas(eventoId);

        comprar(eventoId, "GENERAL", "ana@correo.com").andExpect(status().is(422));
    }

    @Test
    void elAforoMuestraVendidasYCategorias() throws Exception {
        comprar(eventoId, "GENERAL", "ana@correo.com").andExpect(status().isCreated());

        mockMvc.perform(get("/eventos/" + eventoId + "/aforo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aforoMaximo").value(100))
                .andExpect(jsonPath("$.vendidas").value(1))
                .andExpect(jsonPath("$.porcentajeVendido").value(1.0))
                .andExpect(jsonPath("$.aceptaVentas").value(true))
                .andExpect(jsonPath("$.categorias", hasSize(2)));
    }

    @Test
    void elAforoDeUnEventoDesconocidoDevuelve404() throws Exception {
        mockMvc.perform(get("/eventos/" + UUID.randomUUID() + "/aforo")).andExpect(status().isNotFound());
    }
}