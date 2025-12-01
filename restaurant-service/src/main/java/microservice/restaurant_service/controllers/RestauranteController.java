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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import microservice.restaurant_service.dto.RestauranteDTO;
import microservice.restaurant_service.dto.UsuarioCreationDTO;
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
    // @GetMapping
    // public List<Restaurante> obtenerTodos() {
    //     return restauranteService.listarTodos();
    // }

    @GetMapping
    public ResponseEntity<List<RestauranteDTO>> obtenerTodos(@RequestParam(required = false) Long usuarioId) {
        // 💡 required = false es CRÍTICO porque un usuario no logueado (null) también puede ver restaurantes
        // Pasamos el ID (puede ser null o un número) al servicio
        List<RestauranteDTO> lista = restauranteService.listarRestaurantesDTO(usuarioId);
        return ResponseEntity.ok(lista);
    }
    
    // GET /restaurantes/{id}
    @GetMapping("/{id}")
    public ResponseEntity<RestauranteDTO> obtenerPorId(@PathVariable Long id) {
        return restauranteService.obtenerRestauranteDTOPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<RestauranteDTO>> buscar(
        @RequestParam(required = false) String nombre,
        @RequestParam(required = false) String etiqueta
    ) {
        return ResponseEntity.ok(restauranteService.buscarRestaurantes(nombre, etiqueta));
    }   

    // POST /restaurantes
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Restaurante crearRestaurante(@RequestBody RestauranteDTO restaurante) {
        return restauranteService.crearRestaurante(restaurante);
    }

    @PostMapping("/{restauranteId}/gestores") 
    public ResponseEntity<?> crearYAsignarGestor(@PathVariable Long restauranteId, @RequestBody UsuarioCreationDTO gestorDTO) {
        try {
            // Llama al servicio orquestador
            restauranteService.crearYAsignarGestor(restauranteId, gestorDTO); 
            
            // 201 Created indica que la asignación remota fue exitosa
            return new ResponseEntity<>("Gestor creado y asignado exitosamente al restaurante " + restauranteId, HttpStatus.CREATED);
            
        } catch (IllegalArgumentException e) {
            // Falla de validación (Restaurante no existe, o error devuelto por Feign)
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            // Fallo de conexión o error grave en la SAGA.
            return new ResponseEntity<>("Fallo en la creación del Gestor (SAGA): " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // PUT /restaurantes/{id}
    @PutMapping("/{id}")
    // Cambiamos @RequestBody Restaurante a @RequestBody RestauranteDTO
    public ResponseEntity<Restaurante> actualizarRestaurante(@PathVariable Long id, @RequestBody RestauranteDTO restauranteDTO) {
        try {
            // Llamamos al servicio con el DTO
            Restaurante actualizado = restauranteService.actualizarRestaurante(id, restauranteDTO);
            return ResponseEntity.ok(actualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    // @PutMapping("/{id}")
    // public ResponseEntity<Restaurante> actualizarRestaurante(@PathVariable Long id, @RequestBody Restaurante restauranteDetalles) {
    //     try {
    //         Restaurante actualizado = restauranteService.actualizarRestaurante(id, restauranteDetalles);
    //         return ResponseEntity.ok(actualizado);
    //     } catch (RuntimeException e) {
    //         return ResponseEntity.notFound().build();
    //     }
    // }

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
