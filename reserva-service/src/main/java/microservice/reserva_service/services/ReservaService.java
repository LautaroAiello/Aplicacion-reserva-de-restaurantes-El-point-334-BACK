package microservice.reserva_service.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import microservice.reserva_service.entity.Reserva;
import microservice.reserva_service.entity.ReservaMesa;
import microservice.reserva_service.repositories.ReservaRepository;

@Service
public class ReservaService {
    private ReservaRepository reservaRepository;

    public ReservaService(ReservaRepository reservaRepository){
        this.reservaRepository = reservaRepository;
    }

    @Transactional
    public Reserva crearReserva(Reserva reserva) {
        // 1. Lógica de Negocio (Ej: Verificar disponibilidad de mesas - Opcional por ahora)
        boolean disponible = verificarDisponibilidad(reserva.getRestauranteId(), reserva.getFechaHora(), reserva.getCantidadPersonas());
        if (!disponible) throw new RuntimeException("No hay mesas disponibles...");

        // 2. Establecer la fecha de creación y estado inicial
        reserva.setFechaCreacion(LocalDateTime.now());
        if (reserva.getEstado() == null) {
            reserva.setEstado("PENDIENTE"); 
        }

        // 3. Vincular la reserva con sus mesas (si la lista no está vacía)
        if (reserva.getMesasReservadas() != null) {
            for (ReservaMesa rm : reserva.getMesasReservadas()) {
                rm.setReserva(reserva); // Establece la relación bidireccional
            }
        }

        return reservaRepository.save(reserva);
    }

    private boolean verificarDisponibilidad(Long restauranteId, LocalDateTime fechaHora, Integer cantidadPersonas) {
        // Lógica para verificar la disponibilidad de mesas
        return true; // Cambiar esto según la lógica de negocio
    }

    public List<Reserva> obtenerTodas(){
        return reservaRepository.findAll();
    }

    // metodos: getById, cancelarReserva, etc.
}
