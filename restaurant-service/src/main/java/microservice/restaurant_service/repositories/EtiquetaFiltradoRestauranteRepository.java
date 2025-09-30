package microservice.restaurant_service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import microservice.restaurant_service.entity.EtiquetaFiltradoRestaurante;

import java.util.Optional;
import java.util.List;

@Repository
public interface EtiquetaFiltradoRestauranteRepository extends JpaRepository<EtiquetaFiltradoRestaurante, Long> {

    // 1. Encontrar una relación específica por el ID del restaurante y el ID de la etiqueta
    Optional<EtiquetaFiltradoRestaurante> findByRestauranteIdAndEtiquetaId(Long restauranteId, Long etiquetaId);

    // 2. Encontrar todas las etiquetas de filtrado disponibles para un restaurante
    List<EtiquetaFiltradoRestaurante> findByRestauranteId(Long restauranteId);
}