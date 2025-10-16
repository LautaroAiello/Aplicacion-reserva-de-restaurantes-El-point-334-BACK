package com.auth_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "usuario_restaurante_rol", uniqueConstraints = {
    // Garantiza que un usuario solo pueda tener UN rol (GESTOR/ADMIN) por restaurante
    @UniqueConstraint(columnNames = {"usuario_id", "restaurante_id"}) 
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioRestaurante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK Lógica al Usuario-Service
    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId; 

    // FK Lógica a la entidad Restaurante
    @Column(name = "restaurante_id", nullable = false)
    private Long restauranteId; 

    // Rol de gestión: Ej: "ADMIN" (Dueño), "GESTOR"
    @Column(name = "rol", nullable = false, length = 50)
    private String rol; 
}
