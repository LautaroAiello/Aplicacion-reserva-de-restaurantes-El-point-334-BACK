package microservice.reserva_service.services.dto;

import java.time.LocalTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RestauranteDTO {
    private Long id;
    private String nombre;
    // private String direccion;
    private LocalTime horarioApertura;
    private LocalTime horarioCierre;
    private String ImagenUrl;
    
    public String getImagenUrl() {
        return ImagenUrl;
    }
    public void setImagenUrl(String imagenUrl) {
        ImagenUrl = imagenUrl;
    }
    public LocalTime getHorarioApertura() {
        return horarioApertura;
    }
    public void setHorarioApertura(LocalTime horarioApertura) {
        this.horarioApertura = horarioApertura;
    }
    public LocalTime getHorarioCierre() {
        return horarioCierre;
    }
    public void setHorarioCierre(LocalTime horarioCierre) {
        this.horarioCierre = horarioCierre;
    }
    public Long getId() {
        return id;
    }
    public String getNombre() {
        return nombre;
    }
    // public String getDireccion() {
    //     return direccion;
    // }
    public void setId(Long id) {
        this.id = id;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    // public void setDireccion(String direccion) {
    //     this.direccion = direccion;
    // }
}
