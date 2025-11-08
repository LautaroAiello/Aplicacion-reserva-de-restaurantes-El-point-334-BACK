package microservice.reserva_service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import microservice.reserva_service.entity.Reserva;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

	// Obtener reservas por usuario (necesario para la vista "Mis Reservas")
	java.util.List<Reserva> findByUsuarioId(Long usuarioId);

}
