package com.auth_service.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.auth_service.dto.LoginRequest;
import com.auth_service.dto.LoginResponse;
import com.auth_service.entity.Usuario;
import com.auth_service.services.UsuarioService;
import com.auth_service.security.JwtUtil;

import java.util.Optional;

@RestController
@RequestMapping
public class AuthController {

    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(UsuarioService usuarioService, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.usuarioService = usuarioService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Optional<Usuario> opt = usuarioService.getUsuarioByEmail(request.getEmail());
        if (opt.isEmpty()) {
            return new ResponseEntity<>("Credenciales inválidas", HttpStatus.UNAUTHORIZED);
        }

        Usuario usuario = opt.get();
        // Validar que la contraseña venga en el body
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            return new ResponseEntity<>("Credenciales inválidas", HttpStatus.UNAUTHORIZED);
        }

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPasswordHash())) {
            return new ResponseEntity<>("Credenciales inválidas", HttpStatus.UNAUTHORIZED);
        }

        // Obtener roles y roles por restaurante para incluir en el token
        java.util.List<String> roles = usuarioService.getGlobalRoles(usuario.getId());
        java.util.List<com.auth_service.entity.UsuarioRestaurante> rr = usuarioService.getRestauranteRoles(usuario.getId());

        java.util.List<java.util.Map<String,Object>> restauranteRoles = new java.util.ArrayList<>();
        if (rr != null) {
            for (com.auth_service.entity.UsuarioRestaurante ur : rr) {
                java.util.Map<String,Object> m = new java.util.HashMap<>();
                m.put("restauranteId", ur.getRestauranteId());
                m.put("rol", ur.getRol());
                restauranteRoles.add(m);
            }
        }

        String token = jwtUtil.generateToken(usuario.getEmail(), usuario.getId(), roles, restauranteRoles);
        return ResponseEntity.ok(new LoginResponse(token, roles, restauranteRoles));
    }
}
