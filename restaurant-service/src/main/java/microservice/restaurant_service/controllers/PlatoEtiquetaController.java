package microservice.restaurant_service.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import microservice.restaurant_service.entity.PlatoEtiqueta;
import microservice.restaurant_service.services.PlatoEtiquetaService;

import java.util.List;
import java.util.Map;

@RestController
// Ruta base: Gestión de etiquetas en el contexto de un plato
@RequestMapping("/v1/platos/{platoId}/etiquetas") 
public class PlatoEtiquetaController {

    private final PlatoEtiquetaService platoEtiquetaService;

    public PlatoEtiquetaController(PlatoEtiquetaService platoEtiquetaService) {
        this.platoEtiquetaService = platoEtiquetaService;
    }
    
    // GET /v1/platos/{platoId}/etiquetas
    // Muestra todas las etiquetas asociadas al plato
    @GetMapping
    public List<PlatoEtiqueta> obtenerEtiquetasDePlato(@PathVariable Long platoId) {
        return platoEtiquetaService.listarAsociacionesPorPlato(platoId);
    }

    // POST /v1/platos/{platoId}/etiquetas
    // Body: { "etiquetaId": 5 }
    // Crea una nueva asociación (ASOCIAR)
    @PostMapping
    public ResponseEntity<?> asociarEtiqueta(@PathVariable Long platoId, @RequestBody Map<String, Long> payload) {
        Long etiquetaId = payload.get("etiquetaId");
        if (etiquetaId == null) {
            return new ResponseEntity<>("Se requiere 'etiquetaId'.", HttpStatus.BAD_REQUEST);
        }
        
        try {
            PlatoEtiqueta asociacion = platoEtiquetaService.asociarEtiquetaAPlato(platoId, etiquetaId);
            return new ResponseEntity<>(asociacion, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            // Maneja errores como "Plato no encontrado" o "La etiqueta ya está asociada"
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    // DELETE /v1/platos/{platoId}/etiquetas/{etiquetaId}
    // Elimina la asociación (DESASOCIAR)
    @DeleteMapping("/{etiquetaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> desasociarEtiqueta(@PathVariable Long platoId, @PathVariable Long etiquetaId) {
        try {
            platoEtiquetaService.desasociarEtiquetaDePlato(platoId, etiquetaId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}