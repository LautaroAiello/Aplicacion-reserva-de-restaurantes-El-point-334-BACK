package microservice.restaurant_service.repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import microservice.restaurant_service.entity.Plato;

import java.util.List;

@Repository
public interface PlatoRepository extends JpaRepository<Plato, Long> {

    // Encontrar todos los platos de un restaurante específico
    List<Plato> findByRestauranteId(Long restauranteId);

    // Encontrar platos por restaurante y categoría
    List<Plato> findByRestauranteIdAndCategoriaPlatoId(Long restauranteId, Long categoriaId);
}