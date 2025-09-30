package microservice.restaurant_service.services;

import org.springframework.stereotype.Service;

import microservice.restaurant_service.entity.Restaurante;
import microservice.restaurant_service.repositories.RestauranteRepository;

import java.util.List;
import java.util.Optional;

@Service
public class RestauranteService {

    private final RestauranteRepository restauranteRepository;

    //@Autowired
    public RestauranteService(RestauranteRepository restauranteRepository) {
        this.restauranteRepository = restauranteRepository;
    }

    // 1. Obtener todos los restaurantes
    public List<Restaurante> listarTodos() {
        return restauranteRepository.findAll();
    }

    // 2. Obtener un restaurante por ID
    public Optional<Restaurante> buscarPorId(Long id) {
        return restauranteRepository.findById(id);
    }

    // 3. Crear un nuevo restaurante
    public Restaurante guardarRestaurante(Restaurante restaurante) {
        // Lógica de negocio antes de guardar: ej. validaciones de horario, etc.
        return restauranteRepository.save(restaurante);
    }

    // 4. Actualizar un restaurante existente
    public Restaurante actualizarRestaurante(Long id, Restaurante detalles) {
        return restauranteRepository.findById(id)
            .map(restaurante -> {
                // Actualización de campos
                restaurante.setNombre(detalles.getNombre());
                restaurante.setActivo(detalles.getActivo());
                restaurante.setTelefono(detalles.getTelefono());
                restaurante.setHorarioApertura(detalles.getHorarioApertura());
                restaurante.setHorarioCierre(detalles.getHorarioCierre());

                // Al ser el dueño de la FK, al guardar el Restaurante,
                // JPA maneja automáticamente la actualización de Direccion.
                if (detalles.getDireccion() != null) {
                    restaurante.setDireccion(detalles.getDireccion());
                }

                return restauranteRepository.save(restaurante);
            }).orElseThrow(() -> new RuntimeException("Restaurante no encontrado con ID: " + id)); // Manejo de excepciones
    }

    // 5. Eliminar un restaurante
    public void eliminarRestaurante(Long id) {
        restauranteRepository.deleteById(id);
    }
}