package microservice.reserva_service.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import microservice.reserva_service.entity.Reserva;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

	// Obtener reservas por usuario (necesario para la vista "Mis Reservas")
	java.util.List<Reserva> findByUsuarioId(Long usuarioId);

	List<Reserva> findByRestauranteId(Long restauranteId);
    List<Reserva> findByRestauranteIdAndFechaHoraBetween(Long restauranteId, LocalDateTime inicio, LocalDateTime fin);

	@Query("SELECT rm.mesaId FROM Reserva r " +
           "JOIN r.mesasReservadas rm " +
           "WHERE r.restauranteId = :restauranteId " +
           "AND r.fechaHora = :fechaHora " +  // Coincidencia exacta de horario
           "AND r.estado != 'CANCELADA'")     // Ignoramos las canceladas
    List<Long> findMesasOcupadas(
        @Param("restauranteId") Long restauranteId, 
        @Param("fechaHora") LocalDateTime fechaHora
    );

	@Query("SELECT rm.mesaId FROM Reserva r " +
           "JOIN r.mesasReservadas rm " +
           "WHERE r.restauranteId = :restauranteId " +
           // 💡 CAMBIO CLAVE: Buscamos en un RANGO, no hora exacta
           "AND r.fechaHora BETWEEN :inicioRango AND :finRango " +
           "AND r.estado != 'CANCELADA'")
    List<Long> findMesasOcupadasEnRango(
        @Param("restauranteId") Long restauranteId, 
        @Param("inicioRango") LocalDateTime inicioRango,
        @Param("finRango") LocalDateTime finRango
    );
}
