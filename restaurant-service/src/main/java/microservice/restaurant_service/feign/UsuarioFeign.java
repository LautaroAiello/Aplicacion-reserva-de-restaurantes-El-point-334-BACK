package microservice.restaurant_service.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import microservice.restaurant_service.dto.UsuarioCreationDTO;
import microservice.restaurant_service.dto.UsuarioDTO;



@FeignClient(name = "AUTH-SERVICE")
public interface UsuarioFeign {
    
    // Endpoint que el USUARIO-SERVICE debe exponer para crear un nuevo usuario
    @PostMapping("/usuarios/admin-asignacion") 
    UsuarioDTO crearUsuarioYAsignarRol(@RequestBody UsuarioCreationDTO usuarioData);
}