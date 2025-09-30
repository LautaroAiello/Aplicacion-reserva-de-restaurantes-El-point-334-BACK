package microservice.restaurant_service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import microservice.restaurant_service.entity.PlatoEtiqueta;

import java.util.Optional;
import java.util.List;

@Repository
public interface PlatoEtiquetaRepository extends JpaRepository<PlatoEtiqueta, Long> {

    // 1. Encontrar una relación específica por el ID del plato y el ID de la etiqueta
    Optional<PlatoEtiqueta> findByPlatoIdAndEtiquetaId(Long platoId, Long etiquetaId);

    // 2. Encontrar todas las etiquetas asociadas a un plato
    List<PlatoEtiqueta> findByPlatoId(Long platoId);
}