package microservice.reserva_service.services.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import microservice.reserva_service.services.dto.ConfiguracionRestauranteDTO;
import microservice.reserva_service.services.dto.MesaDTO;
import microservice.reserva_service.services.dto.RestauranteDTO;

@FeignClient(name = "RESTAURANT-SERVICE")
public interface RestauranteFeign {
    
    @GetMapping("/restaurantes/{restauranteId}/mesas/{mesaId}")
    MesaDTO obtenerMesaPorIdYRestaurante(
        @PathVariable("restauranteId") Long restauranteId, 
        @PathVariable("mesaId") Long mesaId
    ); 

    @GetMapping("/restaurantes/{restauranteId}")
    RestauranteDTO obtenerRestaurantePorId(@PathVariable("restauranteId") Long restauranteId);

    @GetMapping("/restaurantes/{restauranteId}/mesas")
    java.util.List<MesaDTO> listarMesasPorRestaurante(@PathVariable("restauranteId") Long restauranteId);

    @GetMapping("/restaurantes/{id}/configuracion")
    ConfiguracionRestauranteDTO obtenerConfiguracionPorRestauranteId(@PathVariable("id") Long id);
}
