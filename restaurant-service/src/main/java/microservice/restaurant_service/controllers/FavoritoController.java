package microservice.restaurant_service.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import microservice.restaurant_service.dto.RestauranteDTO;
import microservice.restaurant_service.services.RestauranteService;
import java.util.List;

@RestController
@RequestMapping("/favoritos") // http://localhost:8080/api/restaurant/favoritos
@RequiredArgsConstructor
public class FavoritoController {

    private final RestauranteService restauranteService;

    // POST /favoritos/{restauranteId}?usuarioId=123 (Toggle)
    @PostMapping("/{restauranteId}")
    public ResponseEntity<Boolean> toggleFavorito(
            @PathVariable Long restauranteId,
            @RequestParam Long usuarioId) { // Idealmente extraer del Token JWT en Gateway
        boolean esFavorito = restauranteService.toggleFavorito(restauranteId, usuarioId);
        return ResponseEntity.ok(esFavorito);
    }

    // GET /favoritos/mis-favoritos?usuarioId=123
    @GetMapping("/mis-favoritos")
    public ResponseEntity<List<RestauranteDTO>> misFavoritos(@RequestParam Long usuarioId) {
        return ResponseEntity.ok(restauranteService.obtenerMisFavoritos(usuarioId));
    }

    // GET /favoritos/populares?top=5
    @GetMapping("/populares")
    public ResponseEntity<List<RestauranteDTO>> rankingPopulares(@RequestParam(defaultValue = "5") int top) {
        return ResponseEntity.ok(restauranteService.obtenerMasPopulares(top));
    }
}
