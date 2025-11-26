package microservice.restaurant_service.repositories;

import microservice.restaurant_service.entity.Restaurante;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RestauranteRepository extends JpaRepository<Restaurante, Long> {
    Restaurante findByNombreIgnoreCase(String nombre);
    Optional<Restaurante> findByNombre(String nombre);

    @Query("SELECT DISTINCT r FROM Restaurante r " +
       "LEFT JOIN EtiquetaFiltradoRestaurante efr ON efr.restaurante = r " +
       "LEFT JOIN efr.etiqueta e " +
       "WHERE LOWER(r.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')) " + 
       "AND (:etiqueta IS NULL OR e.nombre = :etiqueta)")
List<Restaurante> buscarPorNombreYEtiqueta(
    @Param("nombre") String nombre, 
    @Param("etiqueta") String etiqueta
);
}
