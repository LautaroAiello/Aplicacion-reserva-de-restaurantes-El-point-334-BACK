package microservice.restaurant_service.services;


import org.springframework.stereotype.Service;

import microservice.restaurant_service.entity.Etiqueta;
import microservice.restaurant_service.entity.EtiquetaFiltradoRestaurante;
import microservice.restaurant_service.entity.Restaurante;
import microservice.restaurant_service.repositories.EtiquetaFiltradoRestauranteRepository;

import java.util.List;

@Service
public class EtiquetaFiltradoRestauranteService {

    private final EtiquetaFiltradoRestauranteRepository repository;
    private final RestauranteService restauranteService; // Dependencia para validar Restaurante
    private final EtiquetaService etiquetaService; // Dependencia para validar Etiqueta

    public EtiquetaFiltradoRestauranteService(
        EtiquetaFiltradoRestauranteRepository repository,
        RestauranteService restauranteService,
        EtiquetaService etiquetaService) {
        this.repository = repository;
        this.restauranteService = restauranteService;
        this.etiquetaService = etiquetaService;
    }

    // Listar las etiquetas de filtrado de un restaurante
    public List<EtiquetaFiltradoRestaurante> listarFiltrosPorRestaurante(Long restauranteId) {
        return repository.findByRestauranteId(restauranteId);
    }

    // Método principal: ASOCIAR una etiqueta como filtro al restaurante
    public EtiquetaFiltradoRestaurante asociarEtiquetaFiltro(Long restauranteId, Long etiquetaId) {
        // 1. Validar existencia del Restaurante
        Restaurante restaurante = restauranteService.obtenerRestaurantePorId(restauranteId)
            .orElseThrow(() -> new RuntimeException("Restaurante no encontrado con ID: " + restauranteId));

        // 2. Validar existencia de la Etiqueta
        Etiqueta etiqueta = etiquetaService.buscarPorId(etiquetaId)
            .orElseThrow(() -> new RuntimeException("Etiqueta no encontrada con ID: " + etiquetaId));

        // 3. Verificar si la asociación ya existe
        if (repository.findByRestauranteIdAndEtiquetaId(restauranteId, etiquetaId).isPresent()) {
            throw new IllegalArgumentException("La etiqueta ya está configurada como filtro para este restaurante.");
        }

        // 4. Crear y guardar la asociación
        EtiquetaFiltradoRestaurante nuevaAsociacion = new EtiquetaFiltradoRestaurante();
        nuevaAsociacion.setRestaurante(restaurante);
        nuevaAsociacion.setEtiqueta(etiqueta);

        return repository.save(nuevaAsociacion);
    }

    // Método principal: DESASOCIAR una etiqueta de filtro del restaurante
    public void desasociarEtiquetaFiltro(Long restauranteId, Long etiquetaId) {
        EtiquetaFiltradoRestaurante asociacion = repository.findByRestauranteIdAndEtiquetaId(restauranteId, etiquetaId)
            .orElseThrow(() -> new RuntimeException("Filtro no encontrado para Restaurante ID: " + restauranteId + " y Etiqueta ID: " + etiquetaId));
        
        repository.delete(asociacion);
    }
}