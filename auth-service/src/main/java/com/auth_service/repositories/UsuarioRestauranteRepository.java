package com.auth_service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.auth_service.entity.UsuarioRestaurante;

import java.util.List;

public interface UsuarioRestauranteRepository extends JpaRepository<UsuarioRestaurante, Long> {
    List<UsuarioRestaurante> findByUsuarioIdAndRol(Long usuarioId, String rol); 
}
