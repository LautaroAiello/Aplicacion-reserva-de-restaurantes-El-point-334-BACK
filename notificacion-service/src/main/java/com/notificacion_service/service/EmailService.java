package com.notificacion_service.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.notificacion_service.dto.ReservaHechaEvent;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

    // Spring inyecta automáticamente el MailSender configurado
    private final JavaMailSender mailSender; 

    // Inyecta el email "Desde" (From) desde el application.properties
    @Value("${spring.mail.username}")
    private String fromEmail; 

    /**
     * Método genérico para enviar un email simple.
     */
    private void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        
        // Esta es la línea que envía el correo
        mailSender.send(message); 
    }

    // =========================================================================
    // MÉTODOS ESPECÍFICOS PARA EVENTOS
    // =========================================================================

    /**
     * Envía la confirmación de la reserva.
     */
    public void enviarConfirmacion(ReservaHechaEvent event) {
        String subject = "✅ ¡Reserva Confirmada en " + event.getRestauranteNombre() + "!";
        
        // Formateo del cuerpo del email
        String body = String.format(
            "¡Hola!\n\nTu reserva ha sido confirmada con éxito.\n" +
            "-----------------------------------\n" +
            "Restaurante: %s\n" +
            "Fecha y Hora: %s\n" +
            "Personas: %d\n" +
            "ID de Reserva: %d\n" +
            "-----------------------------------\n" +
            "\n¡Te esperamos!",
            event.getRestauranteNombre(),
            event.getFechaHora().toString(), // Podrías formatear esto mejor
            event.getNumeroPersonas(),
            event.getReservaId()
        );
        
        sendEmail(event.getEmailUsuario(), subject, body);
    }

    /**
     * Envía la cancelación de la reserva (para implementar a futuro).
     */
    public void enviarCancelacion(String emailUsuario, Long reservaId, String restauranteNombre) {
        String subject = "❌ Reserva Cancelada en " + restauranteNombre;
        String body = String.format(
            "Hola,\n\nLamentamos informarte que tu reserva (ID %d) en %s ha sido cancelada.\n\n" +
            "Si no solicitaste esta cancelación, por favor, contacta al restaurante.",
            reservaId,
            restauranteNombre
        );
        sendEmail(emailUsuario, subject, body);
    }
}