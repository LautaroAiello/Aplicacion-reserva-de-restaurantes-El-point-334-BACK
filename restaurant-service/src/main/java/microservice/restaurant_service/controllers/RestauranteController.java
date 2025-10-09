package microservice.restaurant_service.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import microservice.restaurant_service.dto.RestauranteDTO;
import microservice.restaurant_service.entity.Restaurante;
import microservice.restaurant_service.services.RestauranteService;


@RestController
@RequestMapping("/restaurantes")
public class RestauranteController {
     private final RestauranteService restauranteService;

    public RestauranteController(RestauranteService restauranteService) {
        this.restauranteService = restauranteService;
    }

    // GET /restaurantes
    @GetMapping
    public List<Restaurante> obtenerTodos() {
        return restauranteService.listarTodos();
    }

    // GET /restaurantes/{id}
    @GetMapping("/{id}")
    public ResponseEntity<RestauranteDTO> obtenerPorId(@PathVariable Long id) {
        return restauranteService.obtenerRestauranteDTOPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /restaurantes
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Restaurante crearRestaurante(@RequestBody Restaurante restaurante) {
        return restauranteService.crearRestaurante(restaurante);
    }

    // PUT /restaurantes/{id}
    @PutMapping("/{id}")
    public ResponseEntity<Restaurante> actualizarRestaurante(@PathVariable Long id, @RequestBody Restaurante restauranteDetalles) {
        try {
            Restaurante actualizado = restauranteService.actualizarRestaurante(id, restauranteDetalles);
            return ResponseEntity.ok(actualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE /restaurantes/{id}
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> eliminarRestaurante(@PathVariable Long id) {
        if (restauranteService.obtenerRestaurantePorId(id).isPresent()) {
            restauranteService.eliminarRestaurante(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
