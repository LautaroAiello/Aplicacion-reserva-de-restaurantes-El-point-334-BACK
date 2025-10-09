package microservice.restaurant_service.services;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import microservice.restaurant_service.dto.RestauranteDTO;
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
    @Transactional
    public Optional<Restaurante> obtenerRestaurantePorId(Long id) {
        return restauranteRepository.findById(id);
    }

    public Optional<RestauranteDTO> obtenerRestauranteDTOPorId(Long id) {
    return restauranteRepository.findById(id)
        .map(restaurante -> {
            // 🔑 Mapeo ocurre en el Service (o en una clase Mapper inyectada)
            RestauranteDTO dto = new RestauranteDTO();
                    dto.setId(restaurante.getId());
                    dto.setNombre(restaurante.getNombre());
                    dto.setActivo(restaurante.getActivo());
                    dto.setTelefono(restaurante.getTelefono());
                    dto.setHorarioApertura(restaurante.getHorarioApertura());
                    dto.setHorarioCierre(restaurante.getHorarioCierre());
                    dto.setEntidad_fiscal_id(restaurante.getEntidad_fiscal_id());
                    return dto;
            });
}


    // 3. Crear un nuevo restaurante
    @Transactional
    public Restaurante crearRestaurante(Restaurante restaurante) {
        
        // --- 1. VALIDACIÓN BÁSICA Y NEGOCIO ---
        
        if (restaurante.getNombre() == null || restaurante.getDireccion() == null) {
             throw new IllegalArgumentException("El nombre y la dirección del restaurante son obligatorios.");
        }
        
        // Verificar unicidad del nombre
        Optional<Restaurante> existente = restauranteRepository.findByNombre(restaurante.getNombre());
        if (existente.isPresent()) {
            throw new RuntimeException("Ya existe un restaurante con el nombre: " + restaurante.getNombre());
        }
        
        // --- 2. CONFIGURACIÓN DE CAMPOS Y RELACIONES ---
        
        if (restaurante.getActivo() == null) {
            restaurante.setActivo(true);
        }

        // --- 3. PERSISTENCIA ---
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