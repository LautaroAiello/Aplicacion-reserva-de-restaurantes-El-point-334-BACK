package microservice.restaurant_service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import microservice.restaurant_service.entity.Mesa;

import java.util.List;

@Repository
public interface MesaRepository extends JpaRepository<Mesa, Long> {

    // Método personalizado: Encontrar todas las mesas de un restaurante específico
    List<Mesa> findByRestauranteId(Long restauranteId);
}