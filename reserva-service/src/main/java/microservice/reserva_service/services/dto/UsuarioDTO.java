package microservice.reserva_service.services.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UsuarioDTO {
    private Long id;
    private String nombre;
    private String email;
    private String telefono;

    // private java.util.List<String> roles;
    // private java.util.List<RestauranteRoleDTO> restauranteRoles;

    public String getTelefono() {
        return telefono;
    }
    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
    public Long getId() {
        return id;
    }
    public String getNombre() {
        return nombre;
    }
    public String getEmail() {
        return email;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    // public java.util.List<String> getRoles() {
    //     return roles;
    // }

    // public void setRoles(java.util.List<String> roles) {
    //     this.roles = roles;
    // }

    // public java.util.List<RestauranteRoleDTO> getRestauranteRoles() {
    //     return restauranteRoles;
    // }

    // public void setRestauranteRoles(java.util.List<RestauranteRoleDTO> restauranteRoles) {
    //     this.restauranteRoles = restauranteRoles;
    // }
}
