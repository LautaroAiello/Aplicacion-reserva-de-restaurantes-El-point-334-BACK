package auth_service.microservice.services;

import java.util.List;

import org.springframework.stereotype.Service;

import auth_service.microservice.entity.Usuario;
import auth_service.microservice.repositories.UsuarioRepository;

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
