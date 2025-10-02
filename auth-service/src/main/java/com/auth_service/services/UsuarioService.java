package com.auth_service.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.auth_service.entity.Usuario;
import com.auth_service.repositories.UsuarioRepository;

@Service    
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository){
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario saveUsuario(Usuario usuario){
        return usuarioRepository.save(usuario);
    }

    public List<Usuario> getAllUsuarios(){
        return usuarioRepository.findAll();
    }
}
