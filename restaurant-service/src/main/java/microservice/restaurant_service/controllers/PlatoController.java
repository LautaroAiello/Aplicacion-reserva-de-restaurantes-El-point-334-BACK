package microservice.restaurant_service.controllers;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import microservice.restaurant_service.entity.Plato;
import microservice.restaurant_service.repositories.PlatoRepository;
import microservice.restaurant_service.services.PlatoService;

import java.util.List;

@RestController
@RequestMapping("/restaurantes/{restauranteId}/platos")
public class PlatoController {
    private final PlatoRepository platoRepository;
    private final PlatoService platoService;

   
    public PlatoController(PlatoService platoService, PlatoRepository platoRepository) {
        this.platoService = platoService;
        this.platoRepository = platoRepository;
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

    // PUT: Actualizar plato
    @PutMapping("/{id}") // La ruta completa será /restaurantes/{rId}/platos/{id}
    public ResponseEntity<Plato> actualizarPlato(@PathVariable Long id, @RequestBody Plato platoDetalles) {
        // Aquí deberías llamar a tu servicio, ej: platoService.actualizar(...)
        // Si no tienes servicio intermedio, lo hacemos directo con el repo:
        return platoRepository.findById(id)
            .map(plato -> {
                plato.setNombre(platoDetalles.getNombre());
                plato.setDescripcion(platoDetalles.getDescripcion());
                plato.setPrecio(platoDetalles.getPrecio());
                plato.setEstado(platoDetalles.getEstado());
                plato.setImagenUrl(platoDetalles.getImagenUrl());
                
                // Actualizar categoría si viene
                if (platoDetalles.getCategoriaPlato() != null) {
                    plato.setCategoriaPlato(platoDetalles.getCategoriaPlato());
                }
                
                return ResponseEntity.ok(platoRepository.save(plato));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    // DELETE: Eliminar plato
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPlato(@PathVariable Long id) {
        if (platoRepository.existsById(id)) {
            platoRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
