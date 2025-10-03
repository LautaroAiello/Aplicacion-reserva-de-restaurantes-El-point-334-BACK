package microservice.restaurant_service.controllers;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import microservice.restaurant_service.entity.Mesa;
import microservice.restaurant_service.services.MesaService;

import java.util.List;

@RestController
@RequestMapping("/restaurantes/{restauranteId}/mesas")
public class MesaController {

    private final MesaService mesaService;

    public MesaController(MesaService mesaService) {
        this.mesaService = mesaService;
    }

    // GET /v1/restaurantes/{restauranteId}/mesas
    @GetMapping
    public List<Mesa> obtenerMesasPorRestaurante(@PathVariable Long restauranteId) {
        return mesaService.listarMesasPorRestaurante(restauranteId);
    }

    // Nuevo GET para obtener una sola mesa:
    // GET /v1/restaurantes/{restauranteId}/mesas/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Mesa> obtenerMesaPorId(@PathVariable Long restauranteId, @PathVariable Long id) {
        return mesaService.obtenerMesaId(id)
            .map(mesa->ResponseEntity.ok(mesa))
            .orElse(ResponseEntity.notFound().build());
    }

    // POST /v1/restaurantes/{restauranteId}/mesas
    @PostMapping
    public ResponseEntity<?> crearMesa(@PathVariable Long restauranteId, @RequestBody Mesa mesa) {
        try {
            Mesa nuevaMesa = mesaService.guardarMesa(restauranteId, mesa);
            return new ResponseEntity<>(nuevaMesa, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            // Maneja el caso de Restaurante no encontrado o errores de validación
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // DELETE /v1/restaurantes/{restauranteId}/mesas/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarMesa(@PathVariable Long id) {
        mesaService.eliminarMesa(id);
        return ResponseEntity.noContent().build();
    }
}