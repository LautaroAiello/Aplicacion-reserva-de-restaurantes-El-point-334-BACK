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


    public void enviarRechazo(ReservaHechaEvent event) {
        String subject = "⚠️ Actualización sobre tu reserva en " + event.getRestauranteNombre();
        String body = String.format(
            "Hola,\n\n" +
            "Lamentamos informarte que tu solicitud de reserva no ha podido ser aceptada en esta ocasión.\n" +
            "-----------------------------------\n" +
            "Restaurante: %s\n" +
            "Fecha solicitada: %s\n" +
            "-----------------------------------\n" +
            "Por favor, intenta seleccionar otro horario o contáctanos directamente.\n\nSaludos.",
            event.getRestauranteNombre(),
            event.getFechaHora().toString()
        );
        sendEmail(event.getEmailUsuario(), subject, body);
    }

    public void enviarCancelacion(ReservaHechaEvent event) {
        String subject = "❌ Reserva Cancelada: " + event.getRestauranteNombre();
        String body = String.format(
            "Hola,\n\n" +
            "Te confirmamos que tu reserva ha sido CANCELADA.\n" +
            "-----------------------------------\n" +
            "Restaurante: %s\n" +
            "ID Reserva: %d\n" +
            "-----------------------------------\n" +
            "Esperamos verte en otra oportunidad.",
            event.getRestauranteNombre(),
            event.getReservaId()
        );
        sendEmail(event.getEmailUsuario(), subject, body);
    }
}