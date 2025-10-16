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

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {
    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService){
        this.usuarioService = usuarioService;
    }


    @PostMapping
    public Usuario createUsuario(@RequestBody Usuario usuario) {
        return usuarioService.guardarUsuario(usuario);
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
    

}
