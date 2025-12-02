package microservice.restaurant_service.feign;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import microservice.restaurant_service.dto.UsuarioCreationDTO;
import microservice.restaurant_service.dto.UsuarioDTO;



@FeignClient(name = "AUTH-SERVICE")
public interface UsuarioFeign {
    
    // Endpoint que el USUARIO-SERVICE debe exponer para crear un nuevo usuario
    @PostMapping("/usuarios/admin-asignacion") 
    UsuarioDTO crearUsuarioYAsignarRol(@RequestBody UsuarioCreationDTO usuarioData);
     // NUEVO: Traer lista
    @GetMapping("/usuarios/restaurante/{restauranteId}/rol/{rol}")
    List<UsuarioDTO> obtenerUsuariosPorRestauranteYRol(@PathVariable("restauranteId") Long restauranteId, @PathVariable("rol") String rol);

    // NUEVO: Eliminar
    @DeleteMapping("/usuarios/restaurante/{restauranteId}/gestor/{usuarioId}")
    void eliminarGestor(@PathVariable("restauranteId") Long restauranteId, @PathVariable("usuarioId") Long usuarioId);



}