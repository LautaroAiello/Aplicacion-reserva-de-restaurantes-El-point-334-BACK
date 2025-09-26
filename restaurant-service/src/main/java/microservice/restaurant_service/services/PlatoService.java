package microservice.restaurant_service.services;


import org.springframework.stereotype.Service;

import microservice.restaurant_service.entity.Plato;
import microservice.restaurant_service.entity.Restaurante;
import microservice.restaurant_service.repositories.PlatoRepository;

import java.util.List;

@Service
public class PlatoService {

    private final PlatoRepository platoRepository;
    private final RestauranteService restauranteService;
    // Se necesitaría un CategoriaPlatoService aquí, pero lo omitimos por ahora

    public PlatoService(PlatoRepository platoRepository, RestauranteService restauranteService) {
        this.platoRepository = platoRepository;
        this.restauranteService = restauranteService;
    }

    // Listar platos por Restaurante ID
    public List<Plato> listarPlatosPorRestaurante(Long restauranteId) {
        return platoRepository.findByRestauranteId(restauranteId);
    }

    // Crear un nuevo plato
    public Plato guardarPlato(Long restauranteId, Plato plato) {
        // Lógica de negocio: 1. Verificar si el restaurante existe
        Restaurante restaurante = restauranteService.buscarPorId(restauranteId)
            .orElseThrow(() -> new RuntimeException("Restaurante no encontrado con ID: " + restauranteId));

        plato.setRestaurante(restaurante);

        // Lógica de negocio: 2. Si el plato tiene una categoría, verificar que exista (idealmente)
        // Por ahora, solo guardamos.
        
        return platoRepository.save(plato);
    }
}