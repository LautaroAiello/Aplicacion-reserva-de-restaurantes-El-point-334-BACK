package microservice.reserva_service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import microservice.reserva_service.entity.ReservaMesa;

public interface ReservaMesaRepository extends JpaRepository<ReservaMesa, Long> {
    
}
