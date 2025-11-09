package com.auth_service.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.auth_service.dto.UsuarioAdminCreationDTO;
import com.auth_service.entity.Usuario;
import com.auth_service.entity.UsuarioRestaurante;
import com.auth_service.repositories.UsuarioRepository;
import com.auth_service.repositories.UsuarioRestauranteRepository;

import jakarta.transaction.Transactional;

@Service    
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioRestauranteRepository usuarioRestauranteRepository;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, UsuarioRestauranteRepository usuarioRestauranteRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.usuarioRestauranteRepository = usuarioRestauranteRepository;
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

    @Transactional
    public Usuario crearUsuarioYAsignarRol(UsuarioAdminCreationDTO usuarioAdmin) {
        // 1. Validaciones del Usuario (Ej. Email único, que ya debes tener)
        if (usuarioRepository.findByEmail(usuarioAdmin.getEmail()).isPresent()) {
            throw new IllegalArgumentException("El email " + usuarioAdmin.getEmail() + " ya está registrado.");
        }
    
        // 2. Crear y Guardar el Usuario para obtener su ID
        Usuario nuevoUsuario = new Usuario();
        // ... mapear los campos del request a nuevoUsuario (nombre, email, password, etc.)
        nuevoUsuario.setNombre(usuarioAdmin.getNombre());
        nuevoUsuario.setApellido(usuarioAdmin.getApellido());
        nuevoUsuario.setEmail(usuarioAdmin.getEmail());
        nuevoUsuario.setTelefono(usuarioAdmin.getTelefono());

        String passwordPlana = usuarioAdmin.getPassword();
        String hashedPassword = passwordEncoder.encode(passwordPlana); 
        nuevoUsuario.setPasswordHash(hashedPassword);

    
        Usuario usuarioGuardado = usuarioRepository.save(nuevoUsuario);
        Long usuarioIdGenerado = usuarioGuardado.getId(); // 💡 ¡Aquí tenemos el ID!

        // ----------------------------------------------------------------------
        // 3. Aplicar Restricción del Rol GESTOR (1:1) (POST-CREACIÓN)
        // ----------------------------------------------------------------------
        String rol = usuarioAdmin.getRol().toUpperCase();
    
        if ("GESTOR".equals(rol)) {
            // Usamos el ID generado para validar si ya tiene otra asignación de GESTOR
            if (!usuarioRestauranteRepository.findByUsuarioIdAndRol(usuarioIdGenerado, rol).isEmpty()) {
            
                // ⚠️ Compensación: Si la validación falla AHORA, la entidad Usuario ya está en la BD.
                // Para mantener la atomicidad de la SAGA, deberíamos revertir el Paso 2:
                usuarioRepository.delete(usuarioGuardado); // Eliminar el usuario recién creado
                throw new IllegalArgumentException("Un usuario con rol GESTOR solo puede estar asignado a un restaurante.");
            }
        }
    
        // 4. Asignar el Rol en UsuarioRestaurante
        UsuarioRestaurante asignacion = new UsuarioRestaurante();
        asignacion.setUsuarioId(usuarioIdGenerado); // Usamos el ID generado
        asignacion.setRestauranteId(usuarioAdmin.getRestauranteId());
        asignacion.setRol(rol); 

        usuarioRestauranteRepository.save(asignacion);
    
        return usuarioGuardado;
    }

    public List<Usuario> getAllUsuarios(){
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> getUsuarioById(Long id){
        return usuarioRepository.findById(id);
    }

    public Optional<Usuario> getUsuarioByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    // Devuelve roles globales simples para incluir en tokens
    public List<String> getGlobalRoles(Long usuarioId) {
        List<String> roles = new ArrayList<>();
        // Por defecto todos son USER
        roles.add("USER");
        // Si tiene alguna asignación con rol ADMIN en usuario_restaurante_rol, consideramos ADMIN global
        if (!usuarioRestauranteRepository.findByUsuarioIdAndRol(usuarioId, "ADMIN").isEmpty()) {
            roles.add("ADMIN");
        }
        return roles;
    }

    // Devuelve roles contextuales por restaurante (lista de UsuarioRestaurante)
    public List<UsuarioRestaurante> getRestauranteRoles(Long usuarioId) {
        return usuarioRestauranteRepository.findByUsuarioId(usuarioId);
    }
}