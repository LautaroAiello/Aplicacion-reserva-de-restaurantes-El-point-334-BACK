package microservice.restaurant_service.repositories;

import microservice.restaurant_service.entity.Restaurante;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RestauranteRepository extends JpaRepository<Restaurante, Long> {
    Restaurante findByNombreIgnoreCase(String nombre);
    Optional<Restaurante> findByNombre(String nombre);
}
