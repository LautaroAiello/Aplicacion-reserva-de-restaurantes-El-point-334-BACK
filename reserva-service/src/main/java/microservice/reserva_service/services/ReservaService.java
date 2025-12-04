package microservice.reserva_service.services;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import microservice.reserva_service.services.dto.UsuarioDTO;
import microservice.reserva_service.services.feign.UsuarioFeign;
import microservice.reserva_service.services.feign.RestauranteFeign;
import microservice.reserva_service.services.dto.ConfiguracionRestauranteDTO;
import microservice.reserva_service.services.dto.CrearReservaRequestDTO;
import microservice.reserva_service.services.dto.MesaDTO;
import microservice.reserva_service.services.dto.ReservaHechaEvent;
import microservice.reserva_service.services.dto.ReservaResponseDTO;
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

    /**
     * Endpoint helper: consulta disponibilidad para un restaurante en una fecha/hora.
     * Si se envían mesas específicas (mesaIds) verifica solapamientos con la consulta nativa.
     * Si no se envían mesas, por ahora devuelve true (placeholder) — se puede mejorar consultando
     * restaurant-service para obtener todas las mesas y calcular capacidad.
     */
    public boolean consultarDisponibilidad(Long restauranteId, LocalDateTime fechaHora, Integer cantidadPersonas, java.util.List<Long> mesasIds) {
        LocalDateTime inicioNueva = fechaHora;
        LocalDateTime finNueva = inicioNueva.plusMinutes(240);

        // Si el cliente envía mesas específicas, comprobamos conflictos únicamente sobre esas mesas
        if (mesasIds != null && !mesasIds.isEmpty()) {
            java.util.List<ReservaMesa> conflictos = reservaMesaRepository.findConflictingReservas(mesasIds, inicioNueva, finNueva);
            return conflictos.isEmpty();
        }

        // Si no se envían mesas específicas: obtener todas las mesas del restaurante
        List<MesaDTO> mesas = restauranteFeign.listarMesasPorRestaurante(restauranteId);

        if (mesas == null || mesas.isEmpty()) {
            // Sin mesas registradas, no hay disponibilidad
            return false;
        }

        // Recolectar todos los IDs de mesas y consultar conflictos en bloque
        java.util.List<Long> allMesaIds = mesas.stream().map(MesaDTO::getId).collect(Collectors.toList());
        java.util.List<ReservaMesa> conflictos = reservaMesaRepository.findConflictingReservas(allMesaIds, inicioNueva, finNueva);

        Set<Long> mesasOcupadas = conflictos.stream().map(ReservaMesa::getMesaId).collect(Collectors.toSet());

        // Sumar la capacidad de las mesas libres
        int capacidadLibre = mesas.stream()
                .filter(m -> !mesasOcupadas.contains(m.getId()))
                .mapToInt(MesaDTO::getCapacidad)
                .sum();

        int personasSolicitadas = (cantidadPersonas != null) ? cantidadPersonas : 0;
        return capacidadLibre >= personasSolicitadas;
    }

    public List<Reserva> obtenerTodas(){
        return reservaRepository.findAll();
    }

    public java.util.List<Reserva> obtenerReservasPorUsuario(Long usuarioId) {
        return reservaRepository.findByUsuarioId(usuarioId);
    }

    public Reserva obtenerPorId(Long id) {
        return reservaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Reserva no encontrada con ID: " + id));
    }

    // @Transactional
    // public Reserva crearReserva(Reserva reserva) {
    //     //Obtenemos usuarioId y restauranteId de la reserva
    //     Long userId = reserva.getUsuarioId();
    //     Long restauranteId = reserva.getRestauranteId();
        
        
    //     // Validar que la fecha y hora no sean en el pasado
    //     if (reserva.getFechaHora().isBefore(LocalDateTime.now())) {
    //         throw new RuntimeException("No se puede crear una reserva en el pasado.");
    //     }

    //     // --- 1. VALIDACIÓN DE EXISTENCIA DE RECURSOS (ORQUESTACIÓN) ---

    //     // Validar que el usuario exista en el Auth Service
    //     UsuarioDTO usuario = usuarioFeign.obtenerUsuarioPorId(userId);
    //     if (usuario == null) {
    //          throw new RuntimeException("Usuario con ID " + userId + " no encontrado. No se puede crear la reserva.");
    //     }

    //     // Validar que el restaurante exista en el Restaurant Service
    //     RestauranteDTO restaurante = restauranteFeign.obtenerRestaurantePorId(restauranteId);
    //     if (restaurante == null) {
    //         throw new RuntimeException("Restaurante con ID " + restauranteId + " no encontrado.");
    //     }
    //     // Validar que la reserva incluya al menos una mesa
    //     if (reserva.getMesasReservadas() == null || reserva.getMesasReservadas().isEmpty()) {
    //          throw new RuntimeException("La reserva debe incluir al menos una mesa.");
    //     }

    //     for (ReservaMesa rm : reserva.getMesasReservadas()) {
    //         Long mesaId = rm.getMesaId();
    //         // Llama al Feign Client con ambos IDs para validar la pertenencia
    //         MesaDTO mesa = restauranteFeign.obtenerMesaPorIdYRestaurante(restauranteId, mesaId);
            
    //         if (mesa == null) {
    //              throw new RuntimeException("Mesa con ID " + mesaId + " no existe o no pertenece al Restaurante " + restauranteId + ".");
    //         }
    //         // Establece la relación bidireccional (esto lo tenías bien)
    //         rm.setReserva(reserva); 
    //     }

    //     // --- 2. LÓGICA DE NEGOCIO Y DISPONIBILIDAD ---
    //     validarCapacidadYHorario(reserva, restaurante);
        
    //     // 1. Lógica de Negocio (Ej: Verificar disponibilidad de mesas - Opcional por ahora)
    //     validarDisponibilidadPorSolapamiento(reserva.getMesasReservadas(), reserva.getFechaHora());

    //     // 3. ESTABLECER FECHA DE CREACIÓN Y ESTADO INICIAL
    //     reserva.setFechaCreacion(LocalDateTime.now());
    //     if (reserva.getEstado() == null) {
    //         reserva.setEstado("PENDIENTE"); 
    //     }

    //     // 4. VALIDACIONES COMPLEJAS DE NEGOCIO (CAPACIDAD, HORARIO, ANTICIPACIÓN, ETC.) QUESOOOOOOOOOOO

    //     Reserva reservaGuardada = reservaRepository.save(reserva);

    //     ReservaHechaEvent event = new ReservaHechaEvent(
    //         reservaGuardada.getId(),
    //         restaurante.getNombre(),
    //         reservaGuardada.getFechaHora(),
    //         reservaGuardada.getCantidadPersonas(),
    //         usuario.getEmail(),
    //         usuario.getTelefono()
    //     );

    //     rabbitTemplate.convertAndSend(
    //         RabbitMQReservaConfig.EXCHANGE_NAME, 
    //         RabbitMQReservaConfig.RESERVATION_ROUTING_KEY,  
    //         event
    //     );

    //     return reservaGuardada;
    // }

     // -------------------------------------------------------
    // MÉTODO CREAR RESERVA (OPTIMIZADO Y CORREGIDO)
    // -------------------------------------------------------
@Transactional
    public Reserva crearReserva(CrearReservaRequestDTO request) { // 💡 Recibe el DTO
        
        // --- 1. VALIDACIONES BÁSICAS DEL DTO ---
        if (request.getMesaIds() == null || request.getMesaIds().isEmpty()) {
            throw new IllegalArgumentException("La reserva debe incluir al menos una mesa.");
        }

        if (request.getFechaHora().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("No se puede crear una reserva en el pasado.");
        }

        // --- 2. VALIDACIÓN DE USUARIO (Con manejo de error Feign) ---
        UsuarioDTO usuario = null;
        try {
            usuario = usuarioFeign.obtenerUsuarioPorId(request.getUsuarioId());
        } catch (feign.FeignException.NotFound e) {
            throw new IllegalArgumentException("Usuario con ID " + request.getUsuarioId() + " no encontrado.");
        }

        // --- 3. VALIDACIÓN DE RESTAURANTE ---
        RestauranteDTO restaurante = restauranteFeign.obtenerRestaurantePorId(request.getRestauranteId());
        if (restaurante == null) {
            throw new IllegalArgumentException("Restaurante con ID " + request.getRestauranteId() + " no encontrado.");
        }

        // --- 4. CONSTRUCCIÓN DE LA ENTIDAD RESERVA ---
        Reserva reserva = new Reserva();
        reserva.setUsuarioId(request.getUsuarioId());
        reserva.setRestauranteId(request.getRestauranteId());
        reserva.setFechaHora(request.getFechaHora());
        reserva.setCantidadPersonas(request.getCantidadPersonas());
        reserva.setObservaciones(request.getObservaciones());
        reserva.setTipo(request.getTipo() != null ? request.getTipo() : "NORMAL");
        reserva.setEstado("PENDIENTE");
        reserva.setFechaCreacion(LocalDateTime.now());

        // --- 5. PROCESAMIENTO DE MESAS Y VALIDACIÓN DE CAPACIDAD ---
        List<ReservaMesa> listaReservaMesa = new ArrayList<>();
        int capacidadTotalSeleccionada = 0; 

        for (Long mesaId : request.getMesaIds()) {
            // Validar existencia y obtener datos de la mesa (necesitamos la capacidad)
            MesaDTO mesa = restauranteFeign.obtenerMesaPorIdYRestaurante(request.getRestauranteId(), mesaId);
            
            if (mesa == null) {
                throw new IllegalArgumentException("Mesa " + mesaId + " no existe o no pertenece al restaurante.");
            }

            // Sumar capacidad para validación
            capacidadTotalSeleccionada += mesa.getCapacidad();

            // Crear relación y vincular
            ReservaMesa rm = new ReservaMesa();
            rm.setMesaId(mesaId);
            rm.setReserva(reserva); // 💡 Vinculación bidireccional importante para JPA
            listaReservaMesa.add(rm);
        }

        // Asignar la lista a la reserva
        reserva.setMesasReservadas(listaReservaMesa);

        // 💡 VALIDACIÓN CRÍTICA: ¿Alcanzan las sillas?
        if (capacidadTotalSeleccionada < request.getCantidadPersonas()) {
            throw new IllegalArgumentException(
                "Capacidad insuficiente. Las mesas seleccionadas suman " + capacidadTotalSeleccionada + 
                " lugares, pero la reserva es para " + request.getCantidadPersonas() + " personas."
            );
        }

        // --- 6. VALIDACIONES DE NEGOCIO (Horarios y Solapamiento) ---
        // (Asumiendo que estos métodos existen en tu clase y aceptan la Entidad Reserva)
        validarCapacidadYHorario(reserva, restaurante); 
        validarDisponibilidadPorSolapamiento(reserva.getMesasReservadas(), reserva.getFechaHora());

        // --- 7. GUARDADO EN BD ---
        Reserva reservaGuardada = reservaRepository.save(reserva);

        // --- 8. NOTIFICACIÓN ASÍNCRONA (Resiliente) ---
        try {
            String emailDestino = (request.getEmailCliente() != null && !request.getEmailCliente().isEmpty())
                    ? request.getEmailCliente()
                    : usuario.getEmail();
            
            String telefonoDestino = usuario.getTelefono();

            ReservaHechaEvent event = new ReservaHechaEvent(
                reservaGuardada.getId(),
                restaurante.getNombre(),
                reservaGuardada.getFechaHora(),
                reservaGuardada.getCantidadPersonas(),
                emailDestino,
                telefonoDestino
            );

            rabbitTemplate.convertAndSend(
                RabbitMQReservaConfig.EXCHANGE_NAME, 
                RabbitMQReservaConfig.RESERVATION_ROUTING_KEY,  
                event
            );
            System.out.println("✅ Evento notificacion enviado a RabbitMQ");

        } catch (Exception e) {
            // Si falla RabbitMQ, NO fallamos la reserva, solo logueamos el error
            System.err.println("⚠️ Error al enviar notificación: " + e.getMessage());
        }

        return reservaGuardada;
    }

    // -------------------------------------------------------
    // ACTUALIZAR RESERVA
    // -------------------------------------------------------
    @Transactional
    public Reserva actualizarReserva(Long id, Reserva datosReserva) {
        Reserva reservaExistente = obtenerPorId(id); 

        if (datosReserva.getFechaHora() != null) reservaExistente.setFechaHora(datosReserva.getFechaHora());
        if (datosReserva.getCantidadPersonas() != null) reservaExistente.setCantidadPersonas(datosReserva.getCantidadPersonas());
        if (datosReserva.getObservaciones() != null) reservaExistente.setObservaciones(datosReserva.getObservaciones());
        
        if (datosReserva.getEstado() != null) {
            String estadoAnterior = reservaExistente.getEstado(); 
            String nuevoEstado = datosReserva.getEstado().toUpperCase(); 
            
            reservaExistente.setEstado(nuevoEstado);

            if (!"CONFIRMADA".equals(estadoAnterior) && "CONFIRMADA".equals(nuevoEstado)) {
                enviarNotificacionConfirmacion(reservaExistente);
            }
        }

        if (datosReserva.getMesasReservadas() != null && !datosReserva.getMesasReservadas().isEmpty()) {
            reservaExistente.getMesasReservadas().clear();
            for (ReservaMesa nuevaMesa : datosReserva.getMesasReservadas()) {
                nuevaMesa.setReserva(reservaExistente);
                reservaExistente.getMesasReservadas().add(nuevaMesa);
            }
            // Idealmente aquí también deberíamos re-validar disponibilidad si cambian las mesas
        }

        return reservaRepository.save(reservaExistente);
    }

    // Método auxiliar para notificar confirmaciones
    private void enviarNotificacionConfirmacion(Reserva reserva) {
        try {
            String emailDestino = null;
            String telefonoDestino = null;

            if (reserva.getEmailCliente() != null && !reserva.getEmailCliente().isEmpty()) {
                emailDestino = reserva.getEmailCliente();
            } else {
                UsuarioDTO usuario = usuarioFeign.obtenerUsuarioPorId(reserva.getUsuarioId());
                if (usuario != null) {
                    emailDestino = usuario.getEmail();
                    telefonoDestino = usuario.getTelefono();
                }
            }

            if (emailDestino == null) return;

            String nombreRestaurante = "Restaurante";
            try {
                RestauranteDTO rest = restauranteFeign.obtenerRestaurantePorId(reserva.getRestauranteId());
                if (rest != null) nombreRestaurante = rest.getNombre();
            } catch (Exception e) {}

            ReservaHechaEvent event = new ReservaHechaEvent(
                reserva.getId(),
                nombreRestaurante,
                reserva.getFechaHora(),
                reserva.getCantidadPersonas(),
                emailDestino,
                telefonoDestino
            );

            rabbitTemplate.convertAndSend(
                RabbitMQReservaConfig.EXCHANGE_NAME, 
                RabbitMQReservaConfig.RESERVATION_ROUTING_KEY,  
                event
            );
            System.out.println("✅ Notificación de CONFIRMACIÓN enviada a: " + emailDestino);
            
        } catch (Exception e) {
            System.err.println("Error notificación: " + e.getMessage());
        }
    }

    // // Asegúrate de tener este helper también actualizado:
    // private String extraerEmailDeObservaciones(String obs) {
    //     if (obs == null) return null;
    //     // Regex busca cualquier string con formato de email
    //     java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}");
    //     java.util.regex.Matcher matcher = pattern.matcher(obs);
        
    //     if (matcher.find()) {
    //         return matcher.group();
    //     }
    //     return null;
    // }

    // @Transactional
    // public Reserva actualizarReserva(Long id, Reserva datosReserva) {
    //     // 1. Encontrar la reserva existente o lanzar un error si no existe
    //     Reserva reservaExistente = obtenerPorId(id); 

    //     // 2. Aplicar los cambios necesarios
    //     reservaExistente.setFechaHora(datosReserva.getFechaHora());
    //     reservaExistente.setCantidadPersonas(datosReserva.getCantidadPersonas());
    //     reservaExistente.setEstado(datosReserva.getEstado());
    //     reservaExistente.setObservaciones(datosReserva.getObservaciones());

    //     // Actualizar mesas reservadas (si se envía una nueva lista)
    //     if (datosReserva.getMesasReservadas() != null) {
    //         reservaExistente.getMesasReservadas().clear();
    //         for (ReservaMesa nuevaMesa : datosReserva.getMesasReservadas()) {
    //             nuevaMesa.setReserva(reservaExistente); // Relación bidireccional
    //             reservaExistente.getMesasReservadas().add(nuevaMesa);
    //         }
    //     }

    //     // NOTA: La lógica de actualización de mesas y verificación de disponibilidad
    //     // es compleja y debería ser implementada aquí si se cambian fechas/mesas.

    //     // 3. Guardar y retornar la reserva actualizada
    //     return reservaRepository.save(reservaExistente);
    // }

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
         boolean horarioValido = false;
         // Caso A: Horario Normal (Ej: 09:00 a 18:00) -> Cierre es después de Apertura
        if (horarioCierre.isAfter(horarioApertura)) {
            // Debe estar DENTRO del rango: (hora >= apertura Y hora <= cierre)
            if (!horaReserva.isBefore(horarioApertura) && !horaReserva.isAfter(horarioCierre)) {
                horarioValido = true;
            }
        } 
        // Caso B: Cruza Medianoche (Ej: 20:00 a 00:00 o 20:00 a 02:00) -> Cierre es antes o igual (00:00)
        else {
            // Debe estar EN LOS EXTREMOS: (hora >= apertura O hora <= cierre)
            // Ej: 20:30 >= 20:00 (True) -> Válido
            // Ej: 01:00 <= 02:00 (True) -> Válido
            // Ej: 10:00 no es >= 20:00 y no es <= 02:00 -> Inválido
            if (!horaReserva.isBefore(horarioApertura) || !horaReserva.isAfter(horarioCierre)) {
                horarioValido = true;
            }
        }

        if (!horarioValido) {
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            throw new IllegalArgumentException(
                "El restaurante solo acepta reservas entre las " + horarioApertura.format(timeFormatter) + 
                " y las " + horarioCierre.format(timeFormatter) + "."
            );
        }
          
        // ----------------------------------------------------
        // 3. VALIDACIÓN DE ANTICIPACIÓN Y MÍNIMOS (1.D)
        // ----------------------------------------------------

        ConfiguracionRestauranteDTO configDTO = null;

        try {
        // 💡 Intentamos obtener la configuración
        configDTO = restauranteFeign.obtenerConfiguracionPorRestauranteId(reserva.getRestauranteId());
        } catch (feign.FeignException.NotFound ex) {
        // 💡 SI NO EXISTE (404), NO PASA NADA. Usaremos defaults.
            System.out.println("⚠️ No se encontró configuración para el restaurante " + reserva.getRestauranteId() + ". Usando valores por defecto.");
        // configDTO queda en null
        } catch (Exception ex) {
        // Otros errores (conexión) sí los reportamos
            System.err.println("Error al consultar configuración: " + ex.getMessage());
        }

        final int MINUTOS_ANTICIPACION_REQUERIDOS = (configDTO != null && configDTO.getTiempoAnticipacionMinutos() != null) 
                                                ? configDTO.getTiempoAnticipacionMinutos() 
                                                : 30; // Default: 30 min
    
        final int MIN_PERSONAS_EVENTO_LARGO = (configDTO != null && configDTO.getMinPersonasEventoLargo() != null) 
                                          ? configDTO.getMinPersonasEventoLargo() 
                                          : 10;
    
        // Obtener configuración a través de Feign
        // ConfiguracionRestauranteDTO configDTO = restauranteFeign.obtenerConfiguracionPorRestauranteId(reserva.getRestauranteId());
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

    public List<ReservaResponseDTO> listarReservasPorRestaurante(Long restauranteId) {
        List<Reserva> reservas = reservaRepository.findByRestauranteId(restauranteId);
        
        return reservas.stream().map(reserva -> {
            ReservaResponseDTO dto = new ReservaResponseDTO();
            dto.setId(reserva.getId());
            dto.setFechaHora(reserva.getFechaHora());
            dto.setCantidadPersonas(reserva.getCantidadPersonas());
            dto.setEstado(reserva.getEstado());
            dto.setObservaciones(reserva.getObservaciones());
            dto.setUsuarioId(reserva.getUsuarioId());
            
            // Llamada a Feign para obtener nombre del cliente
            // NOTA: Esto puede ser lento si hay muchas. En prod se cachea o se guarda el nombre en la reserva.
            try {
                // Usamos un token interno o asumimos que la llamada está permitida
                UsuarioDTO usuario = usuarioFeign.obtenerUsuarioPorId(reserva.getUsuarioId()); 
                if (usuario != null) {
                    dto.setNombreCliente(usuario.getNombre());
                    dto.setApellidoCliente(usuario.getApellido());
                }
            } catch (Exception e) {
                dto.setNombreCliente("Usuario");
                dto.setApellidoCliente("Desconocido (" + reserva.getUsuarioId() + ")");
            }
            
            // Mapear mesas
            List<Long> mesasIds = reserva.getMesasReservadas().stream()
                .map(rm -> rm.getMesaId())
                .collect(Collectors.toList());
            dto.setMesasIds(mesasIds);

            return dto;
        }).collect(Collectors.toList());
    }

}
