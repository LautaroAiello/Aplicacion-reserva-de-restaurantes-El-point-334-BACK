package microservice.reserva_service.services.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReservaResponseDTO {
    private Long id;
    private LocalDateTime fechaHora;
    private Integer cantidadPersonas;
    private String estado; // PENDIENTE, CONFIRMADA, RECHAZADA, CANCELADA
    private String observaciones;
    
    // Datos del Cliente (Enriquecidos)
    private Long usuarioId;
    private String nombreCliente;
    private String apellidoCliente;
    private String tipo;
    // Datos de Mesas
    private List<Long> mesasIds; 

    private String nombreRestaurante;
    private String imagenRestauranteUrl;
}
