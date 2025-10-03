package com.auth_service.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.auth_service.entity.Usuario;
import com.auth_service.repositories.UsuarioRepository;

import jakarta.transaction.Transactional;

@Service    
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // public Usuario saveUsuario(Usuario usuario){
    //     return usuarioRepository.save(usuario);
    // }

    @Transactional
    public Usuario guardarUsuario(Usuario usuario) {
        // --- 1. VALIDACIÓN BÁSICA ---
        if (usuario.getEmail() == null || usuario.getPasswordHash() == null) {
            throw new IllegalArgumentException("El email y la contraseña son obligatorios.");
        }
        
        // Opcional: Verificar si el usuario ya existe por email
        if (usuarioRepository.findByEmail(usuario.getEmail()).isPresent()) {
            throw new RuntimeException("El email " + usuario.getEmail() + " ya está en uso.");
        }

        // --- 2. HASHING DE CONTRASEÑA (CRUCIAL) ---
        
        // 1. Obtener la contraseña en texto plano. Asumimos que viene en el campo 'passwordHash'
        String passwordPlana = usuario.getPasswordHash(); 
        
        // 2. Codificar la contraseña. Esto SOLUCIONA el error de NOT NULL.
        String hashedPassword = passwordEncoder.encode(passwordPlana); 
        
        // 3. Reemplazar la contraseña plana por el hash seguro en el objeto a guardar.
        usuario.setPasswordHash(hashedPassword); 
        
        // --- 3. CAMPOS DE NEGOCIO Y ESTADO ---
        
        // Establecer la fecha de registro y el estado inicial
        usuario.setFechaRegistro(LocalDateTime.now());
        if (usuario.getActivo() == null) {
            usuario.setActivo(true); // O el valor que corresponda
        }

        // --- 4. PERSISTENCIA ---
        return usuarioRepository.save(usuario);
    }

    public List<Usuario> getAllUsuarios(){
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> getUsuarioById(Long id){
        return usuarioRepository.findById(id);
    }
}