package microservice.restaurant_service.dto;

public class UsuarioAdminCreationDTO extends UsuarioCreationDTO {
    private Long restauranteId; 
    private String rol;
    public Long getRestauranteId() {
        return restauranteId;
    }
    public void setRestauranteId(Long restauranteId) {
        this.restauranteId = restauranteId;
    }
    public String getRol() {
        return rol;
    }
    public void setRol(String rol) {
        this.rol = rol;
    }        
    
    
}
