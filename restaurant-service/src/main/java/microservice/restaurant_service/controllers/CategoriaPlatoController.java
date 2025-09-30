package microservice.restaurant_service.controllers;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import microservice.restaurant_service.entity.CategoriaPlato;
import microservice.restaurant_service.services.CategoriaPlatoService;

import java.util.List;

@RestController
@RequestMapping("/v1/categorias") // Ruta base independiente
public class CategoriaPlatoController {

    private final CategoriaPlatoService categoriaPlatoService;

    public CategoriaPlatoController(CategoriaPlatoService categoriaPlatoService) {
        this.categoriaPlatoService = categoriaPlatoService;
    }

    // GET /v1/categorias
    @GetMapping
    public List<CategoriaPlato> obtenerTodas() {
        return categoriaPlatoService.listarTodas();
    }

    // GET /v1/categorias/{id}
    @GetMapping("/{id}")
    public ResponseEntity<CategoriaPlato> obtenerPorId(@PathVariable Long id) {
        return categoriaPlatoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /v1/categorias
    @PostMapping
    public ResponseEntity<?> crearCategoria(@RequestBody CategoriaPlato categoria) {
        try {
            CategoriaPlato nuevaCategoria = categoriaPlatoService.guardar(categoria);
            return new ResponseEntity<>(nuevaCategoria, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            // Manejar errores de validación de negocio
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    // DELETE /v1/categorias/{id}
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarCategoria(@PathVariable Long id) {
        categoriaPlatoService.eliminarPorId(id);
    }
}
