package microservice.reserva_service.services;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import microservice.reserva_service.services.dto.UsuarioDTO;
import microservice.reserva_service.services.feign.UsuarioFeign;
import microservice.reserva_service.services.feign.RestauranteFeign;
import microservice.reserva_service.services.dto.ConfiguracionRestauranteDTO;
import microservice.reserva_service.services.dto.MesaDTO;
import microservice.reserva_service.services.dto.ReservaHechaEvent;
import microservice.reserva_service.services.dto.RestauranteDTO;
import microservice.reserva_service.config.RabbitMQReservaConfig;
import microservice.reserva_service.entity.Reserva;
import microservice.reserva_service.entity.ReservaMesa;
import microservice.reserva_service.repositories.ReservaMesaRepository;
import microservice.reserva_service.repositories.ReservaRepository;

import org.springframework.amqp.rabbit.core.RabbitTemplate;

@Service
public class ReservaService {
    private ReservaRepository reservaRepository;
    private UsuarioFeign usuarioFeign;
    private RestauranteFeign restauranteFeign;
    private ReservaMesaRepository reservaMesaRepository;

    private final RabbitTemplate rabbitTemplate;

    public ReservaService(ReservaRepository reservaRepository, RestauranteFeign restauranteFeign, UsuarioFeign usuarioFeign, ReservaMesaRepository reservaMesaRepository, RabbitTemplate rabbitTemplate) {
        this.reservaRepository = reservaRepository;
        this.usuarioFeign = usuarioFeign;
        this.restauranteFeign = restauranteFeign;
        this.reservaMesaRepository = reservaMesaRepository;
        this.rabbitTemplate = rabbitTemplate;
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
        validarCapacidadYHorario(reserva, restaurante);
        
        // 1. Lógica de Negocio (Ej: Verificar disponibilidad de mesas - Opcional por ahora)
        validarDisponibilidadPorSolapamiento(reserva.getMesasReservadas(), reserva.getFechaHora());

        // 3. ESTABLECER FECHA DE CREACIÓN Y ESTADO INICIAL
        reserva.setFechaCreacion(LocalDateTime.now());
        if (reserva.getEstado() == null) {
            reserva.setEstado("PENDIENTE"); 
        }

        // 4. VALIDACIONES COMPLEJAS DE NEGOCIO (CAPACIDAD, HORARIO, ANTICIPACIÓN, ETC.) QUESOOOOOOOOOOO

        Reserva reservaGuardada = reservaRepository.save(reserva);

        ReservaHechaEvent event = new ReservaHechaEvent(
            reservaGuardada.getId(),
            restaurante.getNombre(),
            reservaGuardada.getFechaHora(),
            reservaGuardada.getCantidadPersonas(),
            usuario.getEmail(),
            usuario.getTelefono()
        );

        rabbitTemplate.convertAndSend(
            RabbitMQReservaConfig.EXCHANGE_NAME, 
            RabbitMQReservaConfig.RESERVATION_ROUTING_KEY,  
            event
        );
        System.out.println("✅ Evento ReservaHecha publicado para la Reserva ID: " + reservaGuardada.getId());

        return reservaGuardada;
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

    private void validarDisponibilidadPorSolapamiento(List<ReservaMesa> mesasReservadas, LocalDateTime fechaHora) {    
        List<Long> mesasIds = mesasReservadas.stream()
                .map(ReservaMesa::getMesaId)
                .collect(Collectors.toList());

        LocalDateTime inicioNueva = fechaHora;
        LocalDateTime finNueva = inicioNueva.plusMinutes(240);

        // 🛡️ Búsqueda en el rango de solapamiento (Usando el Repositorio que configuramos)
        List<ReservaMesa> reservasConflicto = reservaMesaRepository
            .findConflictingReservas(
                mesasIds, 
                inicioNueva, // inicioNueva mapea a :inicioNueva en la consulta nativa
                finNueva     // finNueva mapea a :finNueva en la consulta nativa
            );
        
        if (!reservasConflicto.isEmpty()) {
            String mesasEnConflicto = reservasConflicto.stream()
                .map(ReservaMesa::getMesaId)
                .distinct()
                .map(String::valueOf)
                .collect(Collectors.joining(", "));
            
            throw new IllegalArgumentException("Conflicto de disponibilidad. Las mesas " + 
                                               mesasEnConflicto + " ya están reservadas en este horario de 4 horas.");
        }
    }

    
    private void validarCapacidadYHorario(Reserva reserva, RestauranteDTO restaurante) {
    
    // 1. CAPACIDAD Y PERTENENCIA DE MESAS
    
    // Usamos esta variable para acumular la capacidad de todas las mesas solicitadas.
        int capacidadTotalMesas = 0;
    
        if (reserva.getMesasReservadas() == null || reserva.getMesasReservadas().isEmpty()) {
            throw new IllegalArgumentException("La reserva debe incluir al menos una mesa.");
        }

        for (ReservaMesa rm : reserva.getMesasReservadas()) {
            Long mesaId = rm.getMesaId();
        
            // **1.A: Llama al Feign Client para validar existencia y obtener capacidad**
            MesaDTO mesa = restauranteFeign.obtenerMesaPorIdYRestaurante(reserva.getRestauranteId(), mesaId);
            
            if (mesa == null) {
                throw new IllegalArgumentException("Mesa con ID " + mesaId + " no existe o no pertenece al Restaurante " + reserva.getRestauranteId() + ".");
            }
        
            // 1.B: Acumular la capacidad
            capacidadTotalMesas += mesa.getCapacidad(); 

            // 💡 Importante: Establecer la relación bidireccional aquí
            rm.setReserva(reserva);
        }
    
        // **1.C: Comparar capacidad total vs. cantidad de personas**
        if (capacidadTotalMesas < reserva.getCantidadPersonas()) {
            throw new IllegalArgumentException(
                "La capacidad total de las mesas seleccionadas (" + capacidadTotalMesas + 
                " personas) es insuficiente para la cantidad solicitada (" + reserva.getCantidadPersonas() + ")."
            );
        }

        // ----------------------------------------------------
        // 2. VALIDACIÓN DE HORARIO Y ANTICIPACIÓN (1.C y 1.D)
        // ----------------------------------------------------
    
        LocalTime horaReserva = reserva.getFechaHora().toLocalTime();
        LocalTime horarioApertura = restaurante.getHorarioApertura();
        LocalTime horarioCierre = restaurante.getHorarioCierre();
        if (horaReserva.isBefore(horarioApertura) || horaReserva.isAfter(horarioCierre)) {
            // Formateador para mostrar el horario de forma limpia (ej: 12:00)
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

            throw new IllegalArgumentException(
                "El restaurante solo acepta reservas entre las " + horarioApertura.format(timeFormatter) + 
                " y las " + horarioCierre.format(timeFormatter) + "."
            );
        }
          
        // ----------------------------------------------------
        // 3. VALIDACIÓN DE ANTICIPACIÓN Y MÍNIMOS (1.D)
        // ----------------------------------------------------
    
        // Obtener configuración a través de Feign
        ConfiguracionRestauranteDTO configDTO = restauranteFeign.obtenerConfiguracionPorRestauranteId(reserva.getRestauranteId());
    
        // Definición de valores por defecto en caso de que Feign falle (null check)
        // final int MINUTOS_ANTICIPACION_REQUERIDOS = (configDTO != null && configDTO.getTiempoAnticipacionMinutos() != null) 
        //                                             ? configDTO.getTiempoAnticipacionMinutos() 
        //                                             : 30; 
    
        // final int MIN_PERSONAS_EVENTO_LARGO = (configDTO != null && configDTO.getMinPersonasEventoLargo() != null) 
        //                                       ? configDTO.getMinPersonasEventoLargo() 
        //                                       : 10;
    
        // 3.A: Validar Tiempo Mínimo de Anticipación
        LocalDateTime ahora = LocalDateTime.now();
        Integer tiempoAnticipacionMinutos = (configDTO != null && configDTO.getTiempoAnticipacionMinutos() != null) 
                                                    ? configDTO.getTiempoAnticipacionMinutos() 
                                                    : 30;
        Integer minimoPersonasEventoLargo = (configDTO != null && configDTO.getMinPersonasEventoLargo() != null) 
                                                    ? configDTO.getMinPersonasEventoLargo() 
                                                    : 10;

        LocalDateTime tiempoMinimoReserva = ahora.plusMinutes(tiempoAnticipacionMinutos);

        if (reserva.getFechaHora().isBefore(tiempoMinimoReserva)) {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            throw new IllegalArgumentException(
                "La reserva debe realizarse con al menos " + tiempoAnticipacionMinutos + 
                " minutos de anticipación. Hora mínima permitida: " + tiempoMinimoReserva.format(dateTimeFormatter)
            );
        }
    
        // 3.B: Validar Mínimo de Personas para Eventos Largos
        if (reserva.getTipo() != null && reserva.getTipo().equals("EVENTO_LARGO")) {
        
            if (reserva.getCantidadPersonas() < minimoPersonasEventoLargo) {
                 throw new IllegalArgumentException(
                    "Para un 'Evento Largo', la cantidad de personas (" + reserva.getCantidadPersonas() + 
                    ") debe ser igual o superior al mínimo requerido (" + minimoPersonasEventoLargo + ")."
                );
            }
        }

    } 

}
