package microservice.restaurant_service.controllers;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import microservice.restaurant_service.entity.EtiquetaFiltradoRestaurante;
import microservice.restaurant_service.services.EtiquetaFiltradoRestauranteService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/restaurantes/{restauranteId}/filtros") 
public class EtiquetaFiltradoRestauranteController {

    private final EtiquetaFiltradoRestauranteService service;

    public EtiquetaFiltradoRestauranteController(EtiquetaFiltradoRestauranteService service) {
        this.service = service;
    }

    // GET /v1/restaurantes/{restauranteId}/filtros
    // Muestra todas las etiquetas de filtro disponibles para el restaurante
    @GetMapping
    public List<EtiquetaFiltradoRestaurante> obtenerFiltrosDeRestaurante(@PathVariable Long restauranteId) {
        return service.listarFiltrosPorRestaurante(restauranteId);
    }

    // POST /v1/restaurantes/{restauranteId}/filtros
    // Body: { "etiquetaId": 7 } -> ASOCIAR una etiqueta como filtro
    @PostMapping
    public ResponseEntity<?> asociarEtiquetaFiltro(@PathVariable Long restauranteId, @RequestBody Map<String, Long> payload) {
        Long etiquetaId = payload.get("etiquetaId");
        if (etiquetaId == null) {
            return new ResponseEntity<>("Se requiere 'etiquetaId'.", HttpStatus.BAD_REQUEST);
        }
        
        try {
            EtiquetaFiltradoRestaurante asociacion = service.asociarEtiquetaFiltro(restauranteId, etiquetaId);
            return new ResponseEntity<>(asociacion, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // DELETE /v1/restaurantes/{restauranteId}/filtros/{etiquetaId}
    // DESASOCIAR una etiqueta de filtro
    @DeleteMapping("/{etiquetaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> desasociarEtiquetaFiltro(@PathVariable Long restauranteId, @PathVariable Long etiquetaId) {
        try {
            service.desasociarEtiquetaFiltro(restauranteId, etiquetaId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}