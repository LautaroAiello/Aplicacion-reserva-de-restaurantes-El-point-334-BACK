package microservice.reserva_service.services.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MisReservasResponseDTO {
    private Long id;
    private Long restauranteId;
    private LocalDateTime fechaHora;
    private Integer cantidadPersonas;
    private String estado;
    private Long usuarioId;
    private String tipo;
    private String observaciones;
    private String restauranteNombre;
    private String restauranteImagenUrl;
}
