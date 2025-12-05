package com.notificacion_service.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor // Necesario para la deserialización JSON
@AllArgsConstructor // Útil para crear el objeto fácilmente en el Productor
public class ReservaHechaEvent {
    
    private Long reservaId;
    private String restauranteNombre;
    private LocalDateTime fechaHora;
    private int numeroPersonas;
    private String emailUsuario;      // Destino de la notificación por email
    private String telefonoUsuario;   // Destino de la notificación por WhatsApp
    // --- CAMPO NUEVO ---
    private String estadoNuevo; // "CONFIRMADA", "RECHAZADA", "CANCELADA"
}