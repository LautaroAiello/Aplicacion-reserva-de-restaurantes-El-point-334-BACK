package microservice.restaurant_service.dto;

import lombok.Data;

@Data
public class DireccionDTO {
    private String calle;
    private String numero;
    private String ciudad;
    private String provincia;
    private String codigoPostal;
    private String pais;
    private String latitud;
    private String longitud;
}