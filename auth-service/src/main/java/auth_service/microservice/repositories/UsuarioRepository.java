package auth_service.microservice.repositories;

import auth_service.microservice.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long>{
    
}
