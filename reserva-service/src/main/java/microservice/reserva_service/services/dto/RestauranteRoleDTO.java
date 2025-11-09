package microservice.reserva_service.services.dto;

public class RestauranteRoleDTO {
    private Long restauranteId;
    private String rol;

    public RestauranteRoleDTO() {}

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
