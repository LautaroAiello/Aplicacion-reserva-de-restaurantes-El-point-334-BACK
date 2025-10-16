package com.notificacion_service.listeners;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.notificacion_service.config.RabbitMQNotificationConfig;
import com.notificacion_service.dto.ReservaHechaEvent;


@Component
public class ReservaNotificationListener {

    // 💡 Usa el nombre de la Queue que definiste en la configuración
    @RabbitListener(queues = RabbitMQNotificationConfig.NOTIFICATION_QUEUE)
    public void handleReservaNotification(ReservaHechaEvent event) {
        
        System.out.println("---------------------------------------------");
        System.out.println("📧 INICIO DE PROCESO DE NOTIFICACIÓN ASÍNCRONA");
        System.out.println("Reserva ID: " + event.getReservaId());
        System.out.println("Restaurante: " + event.getRestauranteNombre());
        System.out.println("Destino Email: " + event.getEmailUsuario());
        
        // 1. Lógica para Email (Integración con un servicio de Email)
        // Aquí iría tu código para contactar al proveedor de email
        // emailService.enviar(event.getEmailUsuario(), "Confirmación", event);

        // 2. Lógica para WhatsApp (Integración con un API de mensajería)
        if (event.getTelefonoUsuario() != null) {
            System.out.println("Enviando notificación por WhatsApp a: " + event.getTelefonoUsuario());
            // whatsappService.enviar(event.getTelefonoUsuario(), "Su reserva ha sido confirmada.");
        }
        
        // 3. Lógica de LOGGING a MongoDB
        // logService.guardarLogExito(event, "EMAIL");

        System.out.println("FIN DE PROCESO DE NOTIFICACIÓN. El hilo de reserva no fue bloqueado.");
        System.out.println("---------------------------------------------");
    }
}
