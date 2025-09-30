package microservice.restaurant_service.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import microservice.restaurant_service.entity.Etiqueta;
import microservice.restaurant_service.services.EtiquetaService;

import java.util.List;

@RestController
@RequestMapping("/v1/etiquetas") // Ruta base independiente
public class EtiquetaController {

    private final EtiquetaService etiquetaService;

    public EtiquetaController(EtiquetaService etiquetaService) {
        this.etiquetaService = etiquetaService;
    }

    // GET /v1/etiquetas
    @GetMapping
    public List<Etiqueta> obtenerTodas() {
        return etiquetaService.listarTodas();
    }

    // GET /v1/etiquetas/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Etiqueta> obtenerPorId(@PathVariable Long id) {
        return etiquetaService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /v1/etiquetas
    @PostMapping
    public ResponseEntity<?> crearEtiqueta(@RequestBody Etiqueta etiqueta) {
        try {
            Etiqueta nuevaEtiqueta = etiquetaService.guardar(etiqueta);
            return new ResponseEntity<>(nuevaEtiqueta, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    // PUT /v1/etiquetas/{id}
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarEtiqueta(@PathVariable Long id, @RequestBody Etiqueta detalles) {
        try {
            Etiqueta actualizada = etiquetaService.actualizar(id, detalles);
            return ResponseEntity.ok(actualizada);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // DELETE /v1/etiquetas/{id}
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarEtiqueta(@PathVariable Long id) {
        etiquetaService.eliminarPorId(id);
    }
}