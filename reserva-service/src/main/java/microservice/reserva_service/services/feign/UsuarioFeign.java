package microservice.reserva_service.services.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import microservice.reserva_service.services.dto.UsuarioDTO;

@FeignClient(name = "USER-SERVICE")
public interface UsuarioFeign {
    

    @GetMapping("/usuarios/{usuarioId}")
        UsuarioDTO obtenerUsuarioPorId(@PathVariable("usuarioId") Long usuarioId);
}
