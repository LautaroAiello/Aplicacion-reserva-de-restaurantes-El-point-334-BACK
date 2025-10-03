package microservice.reserva_service.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import microservice.reserva_service.services.dto.UsuarioDTO;
import microservice.reserva_service.services.feign.UsuarioFeign;
import microservice.reserva_service.services.feign.RestauranteFeign;
import microservice.reserva_service.services.dto.MesaDTO;
import microservice.reserva_service.services.dto.RestauranteDTO;

import microservice.reserva_service.entity.Reserva;
import microservice.reserva_service.entity.ReservaMesa;
import microservice.reserva_service.repositories.ReservaRepository;

@Service
public class ReservaService {
    private ReservaRepository reservaRepository;
    private UsuarioFeign usuarioFeign;
    private RestauranteFeign restauranteFeign;

    public ReservaService(ReservaRepository reservaRepository, RestauranteFeign restauranteFeign, UsuarioFeign usuarioFeign){
        this.reservaRepository = reservaRepository;
        this.usuarioFeign = usuarioFeign;
        this.restauranteFeign = restauranteFeign;
    }

    public List<Reserva> obtenerTodas(){
        return reservaRepository.findAll();
    }

    public Reserva obtenerPorId(Long id) {
        return reservaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Reserva no encontrada con ID: " + id));
    }

    @Transactional
    public Reserva crearReserva(Reserva reserva) {
        //Obtenemos usuarioId y restauranteId de la reserva
        Long userId = reserva.getUsuarioId();
        Long restauranteId = reserva.getRestauranteId();
        
        // Validar que la fecha y hora no sean en el pasado
        if (reserva.getFechaHora().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("No se puede crear una reserva en el pasado.");
        }

        // --- 1. VALIDACIÓN DE EXISTENCIA DE RECURSOS (ORQUESTACIÓN) ---

        // Validar que el usuario exista en el Auth Service
        UsuarioDTO usuario = usuarioFeign.obtenerUsuarioPorId(userId);
        if (usuario == null) {
             throw new RuntimeException("Usuario con ID " + userId + " no encontrado. No se puede crear la reserva.");
        }
        
        // Validar que el restaurante exista en el Restaurant Service
        RestauranteDTO restaurante = restauranteFeign.obtenerRestaurantePorId(restauranteId);
        if (restaurante == null) {
            throw new RuntimeException("Restaurante con ID " + restauranteId + " no encontrado.");
        }

        // Validar que la reserva incluya al menos una mesa
        if (reserva.getMesasReservadas() == null || reserva.getMesasReservadas().isEmpty()) {
             throw new RuntimeException("La reserva debe incluir al menos una mesa.");
        }

        for (ReservaMesa rm : reserva.getMesasReservadas()) {
            Long mesaId = rm.getMesaId();
            // Llama al Feign Client con ambos IDs para validar la pertenencia
            MesaDTO mesa = restauranteFeign.obtenerMesaPorIdYRestaurante(restauranteId, mesaId);
            
            if (mesa == null) {
                 throw new RuntimeException("Mesa con ID " + mesaId + " no existe o no pertenece al Restaurante " + restauranteId + ".");
            }
            // Establece la relación bidireccional (esto lo tenías bien)
            rm.setReserva(reserva); 
        }

        // --- 2. LÓGICA DE NEGOCIO Y DISPONIBILIDAD ---
        
        // 1. Lógica de Negocio (Ej: Verificar disponibilidad de mesas - Opcional por ahora)
        boolean disponible = verificarDisponibilidad(reserva.getRestauranteId(), reserva.getFechaHora(), reserva.getCantidadPersonas());
        if (!disponible) throw new RuntimeException("No hay mesas disponibles para la fecha y cantidad de personas solicitadas.");
        
        
        
        // 3. ESTABLECER FECHA DE CREACIÓN Y ESTADO INICIAL
        reserva.setFechaCreacion(LocalDateTime.now());
        if (reserva.getEstado() == null) {
            reserva.setEstado("PENDIENTE"); 
        }

        return reservaRepository.save(reserva);
    }


    @Transactional
    public Reserva actualizarReserva(Long id, Reserva datosReserva) {
        // 1. Encontrar la reserva existente o lanzar un error si no existe
        Reserva reservaExistente = obtenerPorId(id); 

        // 2. Aplicar los cambios necesarios
        reservaExistente.setFechaHora(datosReserva.getFechaHora());
        reservaExistente.setCantidadPersonas(datosReserva.getCantidadPersonas());
        reservaExistente.setEstado(datosReserva.getEstado());
        reservaExistente.setObservaciones(datosReserva.getObservaciones());

        // Actualizar mesas reservadas (si se envía una nueva lista)
        if (datosReserva.getMesasReservadas() != null) {
            reservaExistente.getMesasReservadas().clear();
            for (ReservaMesa nuevaMesa : datosReserva.getMesasReservadas()) {
                nuevaMesa.setReserva(reservaExistente); // Relación bidireccional
                reservaExistente.getMesasReservadas().add(nuevaMesa);
            }
        }

        // NOTA: La lógica de actualización de mesas y verificación de disponibilidad
        // es compleja y debería ser implementada aquí si se cambian fechas/mesas.

        // 3. Guardar y retornar la reserva actualizada
        return reservaRepository.save(reservaExistente);
    }

    @Transactional
    public void eliminarReserva(Long id) {
        // 1. Verificar si existe antes de eliminar (para dar una respuesta clara)
        Reserva reservaAEliminar = obtenerPorId(id);

        // 2. Eliminar. Gracias al CascadeType.ALL, también se eliminan las ReservaMesa.
        reservaRepository.delete(reservaAEliminar);
    }

    
    /**
     * Lógica para verificar la disponibilidad. 
     * NOTA: Este método debería interactuar con el microservicio de CATÁLOGO (Mesas).
     */
    private boolean verificarDisponibilidad(Long restauranteId, LocalDateTime fechaHora, Integer cantidadPersonas) {
        // Por ahora, solo devuelve 'true' para permitir la inserción, 
        // pero en un entorno real debe contener la lógica de consulta a la BD.
        
        // Lógica de ejemplo (a implementar):
        // 1. Consultar el servicio de Catálogo por la capacidad total del restaurante.
        // 2. Consultar la tabla RESERVA por todas las reservas confirmadas en ese rango de tiempo.
        // 3. Comparar y retornar.

        return true; 
    }

}
