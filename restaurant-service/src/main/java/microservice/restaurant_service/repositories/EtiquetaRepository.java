package microservice.restaurant_service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import microservice.restaurant_service.entity.Etiqueta;

import java.util.Optional;

@Repository
public interface EtiquetaRepository extends JpaRepository<Etiqueta, Long> {

    // Método para buscar una etiqueta por su nombre, ignorando mayúsculas/minúsculas
    Optional<Etiqueta> findByNombreIgnoreCase(String nombre);
}