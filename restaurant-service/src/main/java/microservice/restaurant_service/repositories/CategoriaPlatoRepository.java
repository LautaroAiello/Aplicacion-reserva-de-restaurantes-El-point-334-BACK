package microservice.restaurant_service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import microservice.restaurant_service.entity.CategoriaPlato;

import java.util.Optional;

@Repository
public interface CategoriaPlatoRepository extends JpaRepository<CategoriaPlato, Long> {

    // Método para evitar duplicados por nombre
    Optional<CategoriaPlato> findByNombreIgnoreCase(String nombre);
}