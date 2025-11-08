package com.notificacion_service.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Representa un Documento en MongoDB.
 * Esta clase define la estructura de los logs de auditoría.
 */
@Document(collection = "logs_de_notificaciones")
@Data
public class NotificacionLog {
    
    @Id
    private String id; // MongoDB usa String como ID por defecto
    
    // --- Trazabilidad del Evento ---
    private Long reservaId; // ID de la reserva (del evento)
    private String origenEvento; // Ej: "RESERVA-SERVICE"
    
    // --- Datos de la Notificación ---
    private String tipoComunicacion; // Ej: "EMAIL", "WHATSAPP"
    private String destinatario;     // El email o número de teléfono
    private String asunto;
    
    // --- Auditoría ---
    private LocalDateTime fechaEnvio;
    private String estado;           // Ej: "ENVIADO", "FALLO_PROVEEDOR"
    private String mensajeError;     // (null si fue exitoso)
    private Object cuerpoMensaje;    // El DTO (ReservaHechaEvent) completo para auditoría
}