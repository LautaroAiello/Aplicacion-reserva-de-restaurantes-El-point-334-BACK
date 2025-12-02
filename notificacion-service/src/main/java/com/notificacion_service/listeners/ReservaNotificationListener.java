package com.notificacion_service.listeners;

import java.time.LocalDateTime;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.notificacion_service.config.RabbitMQNotificationConfig;
import com.notificacion_service.document.NotificacionLog;
import com.notificacion_service.dto.ReservaHechaEvent;
import com.notificacion_service.repository.NotificacionLogRepository;
import com.notificacion_service.service.EmailService;

import lombok.RequiredArgsConstructor;


@Component
@RequiredArgsConstructor
public class ReservaNotificationListener {
    private final NotificacionLogRepository logRepository;
    private final EmailService emailService;

    @RabbitListener(queues = RabbitMQNotificationConfig.NOTIFICATION_QUEUE)
    public void handleReservaNotification(ReservaHechaEvent event) {
        
        System.out.println("---------------------------------------------");
        System.out.println("📧 INICIO DE PROCESO DE NOTIFICACIÓN ASÍNCRONA");
        System.out.println("Reserva ID: " + event.getReservaId());
        
        String tipo = "EMAIL";
        String destinatario = event.getEmailUsuario();
        System.out.println("👉 INTENTANDO ENVIAR A: [" + event.getEmailUsuario() + "]");
        String asunto = "✅ ¡Reserva Confirmada en " + event.getRestauranteNombre() + "!";

        // --- 1. INTENTO DE ENVÍO DE EMAIL (Con try-catch para Resiliencia) ---
        try {
            // LLAMADA REAL A JAVAMAILSENDER (vía EmailService)
            emailService.enviarConfirmacion(event); 
            
            // LOGGING DEL ÉXITO EN MONGODB
            logExito(event, tipo, destinatario, asunto);
            
            System.out.println("✅ Notificación EMAIL enviada y logueada con éxito.");

        } catch (Exception e) {
            // LOGGING DEL FALLO: Captura cualquier error de envío
            System.err.println("❌ ERROR al enviar correo para Reserva ID " + event.getReservaId() + ": " + e.getMessage());
            logFallo(event, tipo, destinatario, asunto, e.getMessage());
        }

        // --- 2. Lógica para WhatsApp (si aplica) ---
        if (event.getTelefonoUsuario() != null) {
            // ... (Lógica similar para WhatsAppService)
        }
        
        System.out.println("---------------------------------------------");
    }
    
    // --- MÉTODOS AUXILIARES PARA LOGGING EN MONGODB ---

    private void logExito(ReservaHechaEvent event, String tipo, String destinatario, String asunto) {
        NotificacionLog log = new NotificacionLog();
        log.setReservaId(event.getReservaId());
        log.setTipoComunicacion(tipo);
        log.setDestinatario(destinatario);
        log.setAsunto(asunto);
        log.setFechaEnvio(LocalDateTime.now());
        log.setEstado("ENVIADO");
        log.setCuerpoMensaje(event); 
        logRepository.save(log);
    }
    
    private void logFallo(ReservaHechaEvent event, String tipo, String destinatario, String asunto, String error) {
        NotificacionLog log = new NotificacionLog();
        log.setReservaId(event.getReservaId());
        log.setTipoComunicacion(tipo);
        log.setDestinatario(destinatario);
        log.setAsunto(asunto);
        log.setFechaEnvio(LocalDateTime.now());
        log.setEstado("FALLO_PROVEEDOR");
        log.setMensajeError(error);
        log.setCuerpoMensaje(event);
        logRepository.save(log);
    }
   
}
