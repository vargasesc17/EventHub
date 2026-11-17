package com.eventhub.boleteria.interfaces.mensajes;

import com.eventhub.boleteria.application.EventoActualizadoComando;
import com.eventhub.boleteria.application.TipoCambio;
import com.eventhub.boleteria.domain.TipoBoleta;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Convierte el JSON que llega por RabbitMQ en comandos para los casos de uso y revisa los campos
 * obligatorios del contrato. Si algo falta o no es válido, lanza {@link MensajeInvalidoException}.
 */
@Component
public class LectorMensajes {

    private final JsonMapper json = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

    public EventoActualizadoComando leerEventoActualizado(String texto) {
        Sobre<DatosEventoActualizado> sobre = leerSobre(texto, "evento.actualizado", new TypeReference<>() { });
        DatosEventoActualizado d = sobre.datos();
        exigir(d.eventoId(), "datos.eventoId");
        exigir(d.cambios(), "datos.cambios");
        exigir(d.nombre(), "datos.nombre");
        exigir(d.fechaInicio(), "datos.fechaInicio");
        exigir(d.lugar(), "datos.lugar");
        exigir(d.aforoMaximo(), "datos.aforoMaximo");
        exigir(d.organizador(), "datos.organizador");
        exigir(d.organizador().organizadorId(), "datos.organizador.organizadorId");
        exigir(d.organizador().correo(), "datos.organizador.correo");
        exigir(d.categorias(), "datos.categorias");
        if (d.cambios().isEmpty() || d.categorias().isEmpty()) {
            throw new MensajeInvalidoException("datos.cambios y datos.categorias no pueden estar vacíos");
        }

        Set<TipoCambio> cambios = EnumSet.noneOf(TipoCambio.class);
        for (String cambio : d.cambios()) {
            cambios.add(enumDe(TipoCambio.class, cambio, "datos.cambios"));
        }
        List<EventoActualizadoComando.Categoria> categorias = new ArrayList<>();
        for (DatosEventoActualizado.Categoria c : d.categorias()) {
            exigir(c.cupos(), "datos.categorias[].cupos");
            exigir(c.precioBase(), "datos.categorias[].precioBase");
            categorias.add(new EventoActualizadoComando.Categoria(
                    enumDe(TipoBoleta.class, c.categoria(), "datos.categorias[].categoria"),
                    c.cupos(), c.precioBase()));
        }

        return new EventoActualizadoComando(sobre.messageId(), d.eventoId(), cambios, d.nombre(), d.fechaInicio(),
                d.lugar(), d.aforoMaximo(), d.organizador().organizadorId(), d.organizador().correo(), categorias);
    }

    /** Lee el sobre común y revisa messageId, tipo y datos. */
    <T> Sobre<T> leerSobre(String texto, String tipoEsperado, TypeReference<Sobre<T>> tipo) {
        Sobre<T> sobre;
        try {
            sobre = json.readValue(texto, tipo);
        } catch (JacksonException e) {
            throw new MensajeInvalidoException("JSON inválido para " + tipoEsperado, e);
        }
        if (sobre == null || sobre.messageId() == null || sobre.datos() == null) {
            throw new MensajeInvalidoException(tipoEsperado + " sin messageId o sin datos");
        }
        if (!tipoEsperado.equals(sobre.tipo())) {
            throw new MensajeInvalidoException("Se esperaba tipo " + tipoEsperado + " y llegó " + sobre.tipo());
        }
        return sobre;
    }

    private static void exigir(Object valor, String campo) {
        if (valor == null) {
            throw new MensajeInvalidoException("Falta el campo obligatorio " + campo);
        }
    }

    private static <E extends Enum<E>> E enumDe(Class<E> tipo, String valor, String campo) {
        try {
            return Enum.valueOf(tipo, valor);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new MensajeInvalidoException("Valor no válido en " + campo + ": " + valor);
        }
    }
}