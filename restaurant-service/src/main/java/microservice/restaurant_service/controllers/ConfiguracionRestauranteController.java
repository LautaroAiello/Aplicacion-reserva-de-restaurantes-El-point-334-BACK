package microservice.restaurant_service.controllers;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import microservice.restaurant_service.entity.ConfiguracionRestaurante;
import microservice.restaurant_service.services.ConfiguracionRestauranteService;

@RestController // Obligatorio para APIs REST
@RequestMapping("/v1/restaurantes/{restauranteId}/configuracion")
public class ConfiguracionRestauranteController {

    private final ConfiguracionRestauranteService configuracionService;

    public ConfiguracionRestauranteController(ConfiguracionRestauranteService configuracionService) {
        this.configuracionService = configuracionService;
    }

    // GET /v1/restaurantes/{restauranteId}/configuracion
    // Obtiene la configuración
    @GetMapping
    public ResponseEntity<ConfiguracionRestaurante> obtenerConfiguracion(@PathVariable Long restauranteId) {
        return configuracionService.buscarPorRestauranteId(restauranteId)
                // Si encuentra la configuración, devuelve 200 OK
                .map(ResponseEntity::ok) 
                // Si no la encuentra, devuelve 404 Not Found
                .orElse(ResponseEntity.notFound().build()); 
    }

    // POST /v1/restaurantes/{restauranteId}/configuracion
    // Crea o actualiza la configuración
    @PostMapping
    public ResponseEntity<?> guardarOActualizarConfiguracion(
            @PathVariable Long restauranteId,
            @RequestBody ConfiguracionRestaurante detalles) {
        try {
            ConfiguracionRestaurante configuracion = configuracionService.guardarOActualizar(restauranteId, detalles);
            return ResponseEntity.ok(configuracion); // Devuelve 200 OK con el objeto guardado
        } catch (RuntimeException e) {
            // Maneja el caso de Restaurante no encontrado
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}