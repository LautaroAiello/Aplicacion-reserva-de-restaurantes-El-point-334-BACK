package microservice.reserva_service.services.dto;

public class MesaDTO {
    
    private Long id;
    private String nombre;
    private int capacidad;
    private Long restauranteId;

    public Long getId() {
        return id;
    }
    public String getNombre() {
        return nombre;
    }
    public int getCapacidad() {
        return capacidad;
    }
    public Long getRestauranteId() {
        return restauranteId;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public void setCapacidad(int capacidad) {
        this.capacidad = capacidad;
    }
    public void setRestauranteId(Long restauranteId) {
        this.restauranteId = restauranteId;
    }

    
}
