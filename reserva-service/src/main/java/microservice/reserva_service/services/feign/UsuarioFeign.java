package microservice.reserva_service.services.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import microservice.reserva_service.services.dto.UsuarioDTO;

@FeignClient(name = "AUTH-SERVICE")
public interface UsuarioFeign {
    

    @GetMapping("/usuarios/{usuarioId}")
        UsuarioDTO obtenerUsuarioPorId(@PathVariable("usuarioId") Long usuarioId);

    @GetMapping("/usuarios/me")
    UsuarioDTO obtenerUsuarioPorToken(@RequestHeader("Authorization") String authorization);
}
