package microservice.restaurant_service.dto;

import lombok.Data;

@Data
public class UsuarioCreationDTO {
    private String nombre;
    private String apellido;
    private String email;
    private String password;
    private String telefono;
}