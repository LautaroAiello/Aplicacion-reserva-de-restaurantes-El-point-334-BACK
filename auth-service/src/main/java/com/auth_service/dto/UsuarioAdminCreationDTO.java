package com.auth_service.dto;

import lombok.Data;

@Data
public class UsuarioAdminCreationDTO {
    // Datos del Usuario (debe coincidir con la entidad Usuario)
    private String nombre;
    private String apellido;
    private String email;
    private String password; 
    private String telefono;

    // Datos de Asignación de Rol
    private Long restauranteId; 
    private String rol; 
}