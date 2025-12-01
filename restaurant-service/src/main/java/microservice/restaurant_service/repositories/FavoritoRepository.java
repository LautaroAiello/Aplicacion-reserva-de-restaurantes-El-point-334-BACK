package microservice.restaurant_service.repositories;

import microservice.restaurant_service.entity.Favorito;
import microservice.restaurant_service.entity.Restaurante;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FavoritoRepository extends JpaRepository<Favorito, Long> {

    // 1. Buscar relación específica (para borrar o chequear)
    Optional<Favorito> findByUsuarioIdAndRestauranteId(Long usuarioId, Long restauranteId);

    // 2. Obtener lista de restaurantes favoritos de un usuario
    @Query("SELECT f.restaurante FROM Favorito f WHERE f.usuarioId = :usuarioId")
    List<Restaurante> findAllByUsuarioId(@Param("usuarioId") Long usuarioId);

    // 3. RANKING DE POPULARIDAD (Top Restaurantes)
    // Cuenta cuántos favoritos tiene cada restaurante y ordena descendente
    @Query("SELECT f.restaurante " +
           "FROM Favorito f " +
           "GROUP BY f.restaurante " +
           "ORDER BY COUNT(f) DESC")
    List<Restaurante> findTopPopulares(Pageable pageable);

    @Query("SELECT f.restaurante.id FROM Favorito f WHERE f.usuarioId = :usuarioId")
    List<Long> findRestauranteIdsByUsuarioId(@Param("usuarioId") Long usuarioId);
}
