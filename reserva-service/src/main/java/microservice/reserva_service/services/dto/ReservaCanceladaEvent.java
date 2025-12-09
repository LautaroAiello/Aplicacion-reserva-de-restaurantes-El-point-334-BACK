package microservice.reserva_service.services.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReservaCanceladaEvent implements Serializable{
    private Long reservaId;
    private String emailDestino;
    private String nombreRestaurante;
}
