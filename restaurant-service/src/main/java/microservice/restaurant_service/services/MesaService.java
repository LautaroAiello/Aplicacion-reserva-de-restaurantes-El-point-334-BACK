package microservice.restaurant_service.services;


import org.springframework.stereotype.Service;

import microservice.restaurant_service.entity.Mesa;
import microservice.restaurant_service.entity.Restaurante;
import microservice.restaurant_service.repositories.MesaRepository;

import java.util.List;
import java.util.Optional;

@Service
public class MesaService {

    private final MesaRepository mesaRepository;
    private final RestauranteService restauranteService; // Para validar la existencia del restaurante

    public MesaService(MesaRepository mesaRepository, RestauranteService restauranteService) {
        this.mesaRepository = mesaRepository;
        this.restauranteService = restauranteService;
    }

    // Listar mesas por Restaurante ID
    public List<Mesa> listarMesasPorRestaurante(Long restauranteId) {
        return mesaRepository.findByRestauranteId(restauranteId);
    }

    // Crear una nueva mesa (debe estar vinculada a un restaurante existente)
    public Mesa guardarMesa(Long restauranteId, Mesa mesa) {
        // Lógica de negocio: 1. Verificar si el restaurante existe
        Optional<Restaurante> restauranteOpt = restauranteService.obtenerRestaurantePorId(restauranteId);

        if (restauranteOpt.isPresent()) {
            mesa.setRestaurante(restauranteOpt.get());
            // Lógica de negocio: 2. Validar que la capacidad sea mayor a 0
            if (mesa.getCapacidad() <= 0) {
                throw new IllegalArgumentException("La capacidad de la mesa debe ser positiva.");
            }
            return mesaRepository.save(mesa);
        } else {
            throw new RuntimeException("Restaurante no encontrado con ID: " + restauranteId);
        }
    }

    // Eliminar mesa
    public void eliminarMesa(Long id) {
        mesaRepository.deleteById(id);
    }

    public Optional<Mesa> obtenerMesaId(Long id) {
        return mesaRepository.findById(id);
    }
}
