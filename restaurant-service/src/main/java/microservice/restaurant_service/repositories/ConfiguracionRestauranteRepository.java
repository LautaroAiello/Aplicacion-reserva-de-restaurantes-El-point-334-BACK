package microservice.restaurant_service.repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import microservice.restaurant_service.entity.ConfiguracionRestaurante;

import java.util.Optional;

@Repository
public interface ConfiguracionRestauranteRepository extends JpaRepository<ConfiguracionRestaurante, Long> {

    // Método para obtener la configuración por el ID de la entidad Restaurante
    Optional<ConfiguracionRestaurante> findByRestauranteId(Long restauranteId);
}
