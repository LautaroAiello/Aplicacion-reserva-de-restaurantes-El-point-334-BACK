package microservice.reserva_service.services.dto;

public class RestauranteDTO {
    private Long id;
    private String nombre;
    private String direccion;
    
    public Long getId() {
        return id;
    }
    public String getNombre() {
        return nombre;
    }
    public String getDireccion() {
        return direccion;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }
}
