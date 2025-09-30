package microservice.restaurant_service.controllers;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import microservice.restaurant_service.entity.Plato;
import microservice.restaurant_service.services.PlatoService;

import java.util.List;

@RestController
@RequestMapping("/restaurantes/{restauranteId}/platos")
public class PlatoController {

    private final PlatoService platoService;

    public PlatoController(PlatoService platoService) {
        this.platoService = platoService;
    }

    // GET /v1/restaurantes/{restauranteId}/platos
    @GetMapping
    public List<Plato> obtenerPlatosPorRestaurante(@PathVariable Long restauranteId) {
        return platoService.listarPlatosPorRestaurante(restauranteId);
    }

    // POST /v1/restaurantes/{restauranteId}/platos
    @PostMapping
    public ResponseEntity<?> crearPlato(@PathVariable Long restauranteId, @RequestBody Plato plato) {
        try {
            Plato nuevoPlato = platoService.guardarPlato(restauranteId, plato);
            return new ResponseEntity<>(nuevoPlato, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            // Maneja el caso de Restaurante no encontrado
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // ... Aquí irían los métodos PUT (actualizar) y DELETE (eliminar)
}
