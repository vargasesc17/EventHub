package com.eventhub.boleteria;

import com.eventhub.boleteria.application.AforoRepository;
import com.eventhub.boleteria.application.InventarioRepository;
import com.eventhub.boleteria.domain.AforoEvento;
import com.eventhub.boleteria.domain.InventarioCategoria;
import com.eventhub.boleteria.domain.TipoBoleta;
import com.eventhub.boleteria.infrastructure.persistencia.BoletaJpaRepository;
import com.eventhub.boleteria.infrastructure.persistencia.CheckInJpaRepository;
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
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** #24 · De punta a punta en Boletería: comprar una boleta y hacer check-in con su QR. */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class CheckInDePuntaAPuntaTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private AforoRepository aforos;
    @Autowired private InventarioRepository inventarios;
    @Autowired private BoletaJpaRepository boletasJpa;
    @Autowired private CheckInJpaRepository checkInsJpa;

    private final UUID eventoId = UUID.randomUUID();
    private final UUID staffId = UUID.randomUUID();

    @BeforeEach
    void eventoALaVenta() {
        aforos.crear(AforoEvento.nuevo(eventoId, "Concierto", Instant.parse("2030-06-01T20:00:00Z"),
                "Movistar Arena", UUID.randomUUID(), "org@correo.com", 100));
        inventarios.crear(List.of(InventarioCategoria.nuevo(eventoId, TipoBoleta.GENERAL, 100, 50_000)));
    }

    /** Compra una boleta General y devuelve el texto de su QR. */
    private String comprarYObtenerQr() throws Exception {
        var cuerpo = """
                {"eventoId": "%s", "categoria": "GENERAL", "compradorId": "%s",
                 "nombreComprador": "Ana Pérez", "correoComprador": "ana@correo.com"}
                """.formatted(eventoId, UUID.randomUUID());
        var respuesta = mockMvc.perform(post("/boletas").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        var m = Pattern.compile("\"codigoQr\"\\s*:\\s*\"([^\"]+)\"").matcher(respuesta);
        assertTrue(m.find(), "La compra debe devolver el codigoQr");
        return m.group(1);
    }

    private ResultActions checkIn(String codigoQr) throws Exception {
        var cuerpo = """
                {"codigoQr": "%s", "staffId": "%s"}
                """.formatted(codigoQr, staffId);
        return mockMvc.perform(post("/checkin").contentType(MediaType.APPLICATION_JSON).content(cuerpo));
    }

    @Test
    void conElQrDeLaCompraEntraUnaSolaVez() throws Exception {
        var qr = comprarYObtenerQr();
        var boletaId = UUID.fromString(qr.substring(0, qr.indexOf('.')));

        checkIn(qr).andExpect(status().isOk())
                .andExpect(jsonPath("$.aceptado").value(true))
                .andExpect(jsonPath("$.categoria").value("GENERAL"));
        checkIn(qr).andExpect(status().isConflict())
                .andExpect(jsonPath("$.aceptado").value(false))
                .andExpect(jsonPath("$.motivo").value("La boleta ya fue usada"));

        assertEquals("USADA", boletasJpa.findById(boletaId).orElseThrow().getEstado().name());
        var registros = checkInsJpa.findByBoletaId(boletaId);
        assertEquals(1, registros.size());
        assertEquals(staffId, registros.get(0).getStaffId());
        assertNotNull(registros.get(0).getFecha());
    }

    @Test
    void unQrAlteradoDevuelve409() throws Exception {
        var qr = comprarYObtenerQr();
        var alterado = qr.substring(0, qr.length() - 1) + (qr.endsWith("A") ? "B" : "A");

        checkIn(alterado).andExpect(status().isConflict())
                .andExpect(jsonPath("$.aceptado").value(false));
    }

    @Test
    void sinStaffDevuelve400() throws Exception {
        mockMvc.perform(post("/checkin").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigoQr\": \"x.y\"}"))
                .andExpect(status().isBadRequest());
    }
}
