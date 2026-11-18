package com.eventhub.notificaciones.interfaces.mensajes;

import com.eventhub.notificaciones.application.AforoCriticoComando;
import com.eventhub.notificaciones.application.BoletaCompradaComando;
import com.eventhub.notificaciones.application.EventoActualizadoComando;
import com.eventhub.notificaciones.domain.InfoEvento;
import com.eventhub.notificaciones.domain.TipoCambio;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.util.EnumSet;
import java.util.Set;

/**
 * Convierte el JSON que llega por RabbitMQ en comandos para los casos de uso y revisa los campos
 * obligatorios del contrato. Si algo falta, lanza {@link MensajeInvalidoException}.
 */
@Component
public class LectorMensajes {

    private final JsonMapper json = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

    public BoletaCompradaComando leerBoletaComprada(String texto) {
        Sobre<DatosBoletaComprada> sobre = leerSobre(texto, "boleta.comprada", new TypeReference<>() { });
        DatosBoletaComprada d = sobre.datos();
        exigir(d.boletaId(), "datos.boletaId");
        exigir(d.eventoId(), "datos.eventoId");
        exigir(d.categoria(), "datos.categoria");
        exigir(d.codigoQr(), "datos.codigoQr");
        exigir(d.comprador(), "datos.comprador");
        exigir(d.comprador().compradorId(), "datos.comprador.compradorId");
        exigir(d.comprador().nombre(), "datos.comprador.nombre");
        exigir(d.comprador().correo(), "datos.comprador.correo");
        exigir(d.evento(), "datos.evento");
        exigir(d.evento().nombre(), "datos.evento.nombre");
        exigir(d.evento().fechaInicio(), "datos.evento.fechaInicio");
        exigir(d.evento().lugar(), "datos.evento.lugar");

        InfoEvento evento = new InfoEvento(d.eventoId(), d.evento().nombre(),
                d.evento().fechaInicio(), d.evento().lugar());
        return new BoletaCompradaComando(sobre.messageId(), d.boletaId(), d.categoria(), d.codigoQr(),
                d.comprador().compradorId(), d.comprador().nombre(), d.comprador().correo(), evento);
    }

    public EventoActualizadoComando leerEventoActualizado(String texto) {
        Sobre<DatosEventoActualizado> sobre = leerSobre(texto, "evento.actualizado", new TypeReference<>() { });
        exigir(sobre.fechaEmision(), "fechaEmision");   // se usa para descartar mensajes viejos
        DatosEventoActualizado d = sobre.datos();
        exigir(d.eventoId(), "datos.eventoId");
        exigir(d.cambios(), "datos.cambios");
        exigir(d.nombre(), "datos.nombre");
        exigir(d.fechaInicio(), "datos.fechaInicio");
        exigir(d.lugar(), "datos.lugar");

        Set<TipoCambio> cambios = EnumSet.noneOf(TipoCambio.class);
        for (String cambio : d.cambios()) {
            try {
                cambios.add(TipoCambio.valueOf(cambio));
            } catch (IllegalArgumentException e) {
                // Un tipo de cambio que no conocemos se ignora: así un valor nuevo no rompe el consumidor.
            }
        }
        var anteriores = d.valoresAnteriores();
        var fechaAnterior = cambios.contains(TipoCambio.FECHA) && anteriores != null ? anteriores.fechaInicio() : null;
        var lugarAnterior = cambios.contains(TipoCambio.LUGAR) && anteriores != null ? anteriores.lugar() : null;

        return new EventoActualizadoComando(sobre.messageId(), sobre.fechaEmision(), cambios,
                new InfoEvento(d.eventoId(), d.nombre(), d.fechaInicio(), d.lugar()),
                fechaAnterior, lugarAnterior);
    }

    public AforoCriticoComando leerAforoCritico(String texto) {
        Sobre<DatosAforoCritico> sobre = leerSobre(texto, "evento.aforo.critico", new TypeReference<>() { });
        DatosAforoCritico d = sobre.datos();
        exigir(d.eventoId(), "datos.eventoId");
        exigir(d.nombreEvento(), "datos.nombreEvento");
        exigir(d.organizador(), "datos.organizador");
        exigir(d.organizador().correo(), "datos.organizador.correo");
        exigir(d.aforoMaximo(), "datos.aforoMaximo");
        exigir(d.vendidas(), "datos.vendidas");
        exigir(d.porcentajeVendido(), "datos.porcentajeVendido");
        exigir(d.umbral(), "datos.umbral");

        return new AforoCriticoComando(sobre.messageId(), d.eventoId(), d.nombreEvento(),
                d.organizador().correo(), d.aforoMaximo(), d.vendidas(), d.porcentajeVendido(), d.umbral());
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
}
