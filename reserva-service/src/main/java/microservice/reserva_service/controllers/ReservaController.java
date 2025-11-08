package microservice.reserva_service.controllers;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import microservice.reserva_service.entity.Reserva;
import microservice.reserva_service.services.ReservaService;
import microservice.reserva_service.services.feign.UsuarioFeign;
import microservice.reserva_service.services.dto.UsuarioDTO;

@RestController
@RequestMapping("/reservas")
public class ReservaController {

    private final ReservaService reservaService;
    private final UsuarioFeign usuarioFeign;

    public ReservaController(ReservaService reservaService, UsuarioFeign usuarioFeign) {
        this.reservaService = reservaService;
        this.usuarioFeign = usuarioFeign;
    }
   
    @PostMapping
    public ResponseEntity<Reserva> crearReserva(@RequestBody Reserva reserva) {
        System.out.println(">>> PETICIÓN RECIBIDA CON ÉXITO EN EL CONTROLADOR. <<<");
        Reserva nuevaReserva = reservaService.crearReserva(reserva);
        return new ResponseEntity<>(nuevaReserva, HttpStatus.CREATED);
    }
    
    @GetMapping
    public ResponseEntity<List<Reserva>> obtenerTodas() {
        List<Reserva> reservas = reservaService.obtenerTodas();
        return new ResponseEntity<>(reservas, HttpStatus.OK);
    }

    // GET /reservas/disponibilidad?restauranteId=1&fechaHora=2025-11-10T20:00:00&cantidadPersonas=4&mesasIds=1,2
    @GetMapping("/disponibilidad")
    public ResponseEntity<?> consultarDisponibilidad(@RequestParam Long restauranteId,
                                                     @RequestParam String fechaHora,
                                                     @RequestParam Integer cantidadPersonas,
                                                     @RequestParam(required = false) String mesasIds) {
        try {
            java.time.LocalDateTime fecha = java.time.LocalDateTime.parse(fechaHora);
            java.util.List<Long> listaMesas = null;
            if (mesasIds != null && !mesasIds.isBlank()) {
                listaMesas = java.util.Arrays.stream(mesasIds.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(Long::valueOf)
                        .toList();
            }

            boolean disponible = reservaService.consultarDisponibilidad(restauranteId, fecha, cantidadPersonas, listaMesas);
            return ResponseEntity.ok(java.util.Map.of("available", disponible));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // GET /reservas/usuario/{usuarioId}
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<Reserva>> obtenerReservasPorUsuario(@PathVariable Long usuarioId) {
        try {
            java.util.List<Reserva> reservas = reservaService.obtenerReservasPorUsuario(usuarioId);
            return ResponseEntity.ok(reservas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // GET /reservas/mias -> obtiene usuario desde token y devuelve sus reservas
    @GetMapping("/mias")
    public ResponseEntity<List<Reserva>> obtenerMisReservas(@RequestHeader(name = "Authorization", required = false) String authorization) {
        try {
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            UsuarioDTO usuario = usuarioFeign.obtenerUsuarioPorToken(authorization);
            if (usuario == null || usuario.getId() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            java.util.List<Reserva> reservas = reservaService.obtenerReservasPorUsuario(usuario.getId());
            return ResponseEntity.ok(reservas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reserva> obtenerPorId(@PathVariable Long id) {
        try {
            Reserva reserva = reservaService.obtenerPorId(id);
            return new ResponseEntity<>(reserva, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // Código 404
        }
    }
    

    //  ENDPOINTS DE MODIFICACIÓN Y ELIMINACIÓN 
    
    @PutMapping("/{id}")
    public ResponseEntity<Reserva> actualizarReserva(@PathVariable Long id, @RequestBody Reserva reserva) {
        try {
            Reserva reservaActualizada = reservaService.actualizarReserva(id, reserva);
            return new ResponseEntity<>(reservaActualizada, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // Código 404
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarReserva(@PathVariable Long id) {
        try {
            reservaService.eliminarReserva(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT); // Código 204
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // Código 404
        }
    }

}
