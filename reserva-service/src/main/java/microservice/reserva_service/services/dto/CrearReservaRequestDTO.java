package microservice.reserva_service.services.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CrearReservaRequestDTO {
    private Long usuarioId;
    private Long restauranteId;
    private LocalDateTime fechaHora;
    private Integer cantidadPersonas;
    private String observaciones;
    private String tipo;
    private List<Long> mesaIds; 
    private String emailCliente;
}
