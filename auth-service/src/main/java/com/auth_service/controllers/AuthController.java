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
import com.auth_service.repositories.UsuarioRepository;
import com.auth_service.security.JwtUtil;

import java.util.Optional;

@RestController
@RequestMapping
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Optional<Usuario> opt = usuarioRepository.findByEmail(request.getEmail());
        if (opt.isEmpty()) {
            return new ResponseEntity<>("Credenciales inválidas", HttpStatus.UNAUTHORIZED);
        }

        Usuario usuario = opt.get();
        if (!passwordEncoder.matches(request.getPassword(), usuario.getPasswordHash())) {
            return new ResponseEntity<>("Credenciales inválidas", HttpStatus.UNAUTHORIZED);
        }

        String token = jwtUtil.generateToken(usuario.getEmail(), usuario.getId());
        return ResponseEntity.ok(new LoginResponse(token));
    }
}
