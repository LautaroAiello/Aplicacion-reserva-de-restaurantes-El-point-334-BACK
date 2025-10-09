package microservice.restaurant_service.services;

import org.springframework.stereotype.Service;

import microservice.restaurant_service.dto.ConfiguracionRestauranteDTO;
import microservice.restaurant_service.entity.ConfiguracionRestaurante;
import microservice.restaurant_service.entity.Restaurante;
import microservice.restaurant_service.repositories.ConfiguracionRestauranteRepository;
import microservice.restaurant_service.repositories.RestauranteRepository;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class ConfiguracionRestauranteService {

    private final ConfiguracionRestauranteRepository configuracionRepository;
    // Necesario para validar la existencia del restaurante padre
    private final RestauranteService restauranteService;
    private final RestauranteRepository restauranteRepository;

    public ConfiguracionRestauranteService(ConfiguracionRestauranteRepository configuracionRepository, RestauranteService restauranteService, RestauranteRepository restauranteRepository) {
        this.configuracionRepository = configuracionRepository;
        this.restauranteService = restauranteService;
        this.restauranteRepository = restauranteRepository;
    }

    // Obtener la configuración de un restaurante específico
    public ConfiguracionRestauranteDTO buscarPorRestauranteId(Long restauranteId) {
        return restauranteRepository.findById(restauranteId)
            .map(restaurante -> {
                ConfiguracionRestaurante config = restaurante.getConfiguracion();
                if (config == null) {
                    throw new NoSuchElementException("Configuración no encontrada para el restaurante ID: " + restauranteId);
                }
                
                // Mapeo de Entidad JPA a DTO
                ConfiguracionRestauranteDTO dto = new ConfiguracionRestauranteDTO();
                dto.setTiempoAnticipacionMinutos(config.getTiempoAnticipacionMinutos());
                dto.setMinPersonasEventoLargo(config.getMinPersonasEventoLargo());
                return dto;
            })
            .orElseThrow(() -> new NoSuchElementException("Restaurante no encontrado con ID: " + restauranteId));
} 

    // Crear o actualizar la configuración
    public ConfiguracionRestaurante guardarOActualizar(Long restauranteId, ConfiguracionRestaurante detalles) {
        // 1. Verificar si el restaurante existe
        Restaurante restaurante = restauranteService.obtenerRestaurantePorId(restauranteId)
            .orElseThrow(() -> new RuntimeException("Restaurante no encontrado con ID: " + restauranteId));

        // 2. Buscar si ya existe una configuración para este restaurante
        Optional<ConfiguracionRestaurante> existingConfig = configuracionRepository.findByRestauranteId(restauranteId);

        ConfiguracionRestaurante configToSave;

        if (existingConfig.isPresent()) {
            // Caso Actualizar (Update)
            configToSave = existingConfig.get();
            // Mapeo manual de campos (Lombok @Data facilita los setters)
            configToSave.setTiempoAnticipacionMinutos(detalles.getTiempoAnticipacionMinutos());
            configToSave.setMinPersonasEventoLargo(detalles.getMinPersonasEventoLargo());
            configToSave.setMapaAncho(detalles.getMapaAncho());
            configToSave.setMapaLargo(detalles.getMapaLargo());
            configToSave.setMostrarPrecios(detalles.getMostrarPrecios());
        } else {
            // Caso Crear (Insert)
            configToSave = detalles;
            configToSave.setRestaurante(restaurante);
        }

        return configuracionRepository.save(configToSave);
    }
    
    // Método para buscar el restaurante por ID (debe estar en RestauranteService)
    // Se asume que RestauranteService ya tiene:
    // public Optional<Restaurante> buscarPorId(Long id) { return restauranteRepository.findById(id); }
}