package microservice.reserva_service.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import feign.Param;
import microservice.reserva_service.entity.ReservaMesa;

public interface ReservaMesaRepository extends JpaRepository<ReservaMesa, Long> {
        /**
     * Usa SQL Nativo de PostgreSQL para calcular la superposición de intervalos de 4 horas.
     * r.fecha_hora es el inicio de la reserva existente.
     * r.fecha_hora + interval '4 hours' es el fin de la reserva existente.
     * La superposición existe si (Inicio Existente < Fin Nueva) Y (Fin Existente > Inicio Nueva).
     */
    @Query(value = "SELECT rm.* FROM reserva_mesa rm " +
                   "JOIN reserva r ON rm.reserva_id = r.id " +
                   "WHERE rm.mesa_id IN :mesasIds AND r.estado IN ('PENDIENTE', 'CONFIRMADA') AND " +
                   // Condición de solapamiento:
                   "r.fecha_hora < :finNueva AND " +
                   "(r.fecha_hora + interval '4 hours') > :inicioNueva", 
           nativeQuery = true)
    List<ReservaMesa> findConflictingReservas(
            @Param("mesasIds") List<Long> mesasIds,
            @Param("inicioNueva") LocalDateTime inicioNueva,
            @Param("finNueva") LocalDateTime finNueva);
}
