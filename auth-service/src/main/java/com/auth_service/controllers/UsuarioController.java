package com.auth_service.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.auth_service.dto.UsuarioAdminCreationDTO;
import com.auth_service.entity.Usuario;
import com.auth_service.services.UsuarioService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import com.auth_service.security.JwtUtil;
import io.jsonwebtoken.Claims;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {
    private final UsuarioService usuarioService;
    private final JwtUtil jwtUtil;

    public UsuarioController(UsuarioService usuarioService, JwtUtil jwtUtil){
        this.usuarioService = usuarioService;
        this.jwtUtil = jwtUtil;
    }


    @PostMapping
    public ResponseEntity<?> createUsuario(@RequestBody com.auth_service.dto.RegisterRequest req) {
        try {
            Usuario usuario = new Usuario();
            usuario.setNombre(req.getNombre());
            usuario.setApellido(req.getApellido());
            usuario.setEmail(req.getEmail());
            // The service expects passwordHash field to contain the plain password; it will hash it.
            usuario.setPasswordHash(req.getPassword());
            usuario.setTelefono(req.getTelefono());

            Usuario saved = usuarioService.guardarUsuario(usuario);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            // Log the exception server-side in a real app; return generic message to client
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno al crear usuario");
        }
    }

    @PostMapping("/admin-asignacion") // 💡 EL ENDPOINT QUE NECESITAMOS
        public ResponseEntity<?> crearUsuarioYAsignarRol(@RequestBody UsuarioAdminCreationDTO usuarioAdmin) {
        try {
            // Asume que tu UsuarioService devuelve un DTO, no la entidad Usuario
            Usuario usuarioCreado = usuarioService.crearUsuarioYAsignarRol(usuarioAdmin);
            // Devolver un DTO de respuesta con el ID generado
            return new ResponseEntity<>(usuarioCreado, HttpStatus.CREATED); 
        } catch (IllegalArgumentException e) {
            // Manejar errores de validación (email duplicado, restricción de gestor)
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("Error interno al crear usuario y asignar rol.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping
    public List<Usuario> getAllUsuarios() {
        return usuarioService.getAllUsuarios();
    }

    @GetMapping("/{id}")
    public Usuario getUsuarioById(@PathVariable Long id) {
        return usuarioService.getUsuarioById(id).orElse(null);
    }   

    /**
     * GET /usuarios/me
     * Accepts Authorization: Bearer <token> and returns the current Usuario
     */
    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestHeader(name = "Authorization", required = false) String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorization header missing or invalid");
        }

        String token = authorization.substring("Bearer ".length());
        Claims claims = jwtUtil.parseClaims(token);
        Object usuarioIdObj = claims.get("usuarioId");
        if (usuarioIdObj == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token missing usuarioId claim");
        }

        Long usuarioId = null;
        if (usuarioIdObj instanceof Integer) {
            usuarioId = ((Integer) usuarioIdObj).longValue();
        } else if (usuarioIdObj instanceof Long) {
            usuarioId = (Long) usuarioIdObj;
        } else if (usuarioIdObj instanceof String) {
            usuarioId = Long.valueOf((String) usuarioIdObj);
        }

        java.util.Optional<Usuario> opt = usuarioService.getUsuarioById(usuarioId);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Usuario u = opt.get();

        java.util.Map<String,Object> resp = new java.util.HashMap<>();
        resp.put("id", u.getId());
        resp.put("nombre", u.getNombre());
        resp.put("apellido", u.getApellido());
        resp.put("email", u.getEmail());
        resp.put("telefono", u.getTelefono());
        resp.put("activo", u.getActivo());
        resp.put("fechaRegistro", u.getFechaRegistro());

        // roles
        java.util.List<String> roles = usuarioService.getGlobalRoles(usuarioId);
        resp.put("roles", roles);

        // restaurante roles
        java.util.List<com.auth_service.entity.UsuarioRestaurante> rr = usuarioService.getRestauranteRoles(usuarioId);
        java.util.List<java.util.Map<String,Object>> restaurantes = new java.util.ArrayList<>();
        if (rr != null) {
            for (com.auth_service.entity.UsuarioRestaurante r : rr) {
                java.util.Map<String,Object> m = new java.util.HashMap<>();
                m.put("restauranteId", r.getRestauranteId());
                m.put("rol", r.getRol());
                restaurantes.add(m);
            }
        }
        resp.put("restauranteRoles", restaurantes);

        return ResponseEntity.ok(resp);
    }
    

}
