package microservice.restaurant_service.dto;

import java.time.LocalTime;

public class RestauranteDTO {
    // --- Datos del Restaurante ---
    private Long id;
    private String nombre;
    private Boolean activo;
    private String telefono;
    private LocalTime horarioApertura;
    private LocalTime horarioCierre;
    private Long entidad_fiscal_id;
    private DireccionDTO direccion;

    // --- Datos del Administrador/Dueño (Usuario) ---
    private String nombreUsuario;
    private String apellidoUsuario;
    private String emailUsuario;
    private String passwordUsuario;
    private String telefonoUsuario;

    public String getNombreUsuario() {
        return nombreUsuario;
    }
    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }
    public String getApellidoUsuario() {
        return apellidoUsuario;
    }
    public void setApellidoUsuario(String apellidoUsuario) {
        this.apellidoUsuario = apellidoUsuario;
    }
    public String getEmailUsuario() {
        return emailUsuario;
    }
    public void setEmailUsuario(String emailUsuario) {
        this.emailUsuario = emailUsuario;
    }
    public String getPasswordUsuario() {
        return passwordUsuario;
    }
    public void setPasswordUsuario(String passwordUsuario) {
        this.passwordUsuario = passwordUsuario;
    }
    public String getTelefonoUsuario() {
        return telefonoUsuario;
    }
    public void setTelefonoUsuario(String telefonoUsuario) {
        this.telefonoUsuario = telefonoUsuario;
    }
    


    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public Boolean getActivo() {
        return activo;
    }
    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
    public String getTelefono() {
        return telefono;
    }
    public void setTelefono(String telefono) {
        this.telefono = telefono;
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
    public Long getEntidad_fiscal_id() {
        return entidad_fiscal_id;
    }
    public void setEntidad_fiscal_id(Long entidad_fiscal_id) {
        this.entidad_fiscal_id = entidad_fiscal_id;
    }
    public DireccionDTO getDireccion() {
        return direccion;
    }
    public void setDireccion(DireccionDTO direccion) {
        this.direccion = direccion;
    }
}
