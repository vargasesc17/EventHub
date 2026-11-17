package com.eventhub.notificaciones.infrastructure.correo;

import com.eventhub.notificaciones.application.EnviadorCorreo;
import com.eventhub.notificaciones.domain.Correo;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * Envía los correos por SMTP. En local apunta a Mailpit (http://localhost:8025), que los atrapa
 * para verlos en el navegador sin mandar nada de verdad.
 */
@Component
public class SmtpEnviadorCorreo implements EnviadorCorreo {

    private final JavaMailSender mailSender;
    private final String remitente;

    public SmtpEnviadorCorreo(JavaMailSender mailSender,
                              @Value("${notificaciones.correo.remitente}") String remitente) {
        this.mailSender = mailSender;
        this.remitente = remitente;
    }

    @Override
    public void enviar(Correo correo) {
        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, StandardCharsets.UTF_8.name());
            helper.setFrom(remitente);
            helper.setTo(correo.para());
            helper.setSubject(correo.asunto());
            helper.setText(correo.cuerpoHtml(), true);
            if (correo.tieneQr()) {
                helper.addInline("qr", new ByteArrayResource(correo.qrPng()), "image/png");
            }
            mailSender.send(mensaje);
        } catch (MessagingException e) {
            // Al lanzar la excepción, el consumidor reintenta (y si sigue fallando, va a la DLQ).
            throw new IllegalStateException("No se pudo armar el correo para " + correo.para(), e);
        }
    }
}
