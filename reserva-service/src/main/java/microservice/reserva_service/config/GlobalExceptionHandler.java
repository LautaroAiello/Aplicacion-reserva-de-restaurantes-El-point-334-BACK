package microservice.reserva_service.config;

import microservice.reserva_service.services.dto.ErrorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

// Excepciones de Feign
import feign.FeignException;

@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 1. Maneja las excepciones de validación de negocio (lanzadas con IllegalArgumentException).
     * Ej: Conflicto de solapamiento, Capacidad insuficiente, Horario no válido.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationException(IllegalArgumentException ex, WebRequest request) {
        
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
            HttpStatus.BAD_REQUEST.value(),
            "Validation Error",
            ex.getMessage(), 
            request.getDescription(false).replace("uri=", "")
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST); // HTTP 400
    }

    /**
     * 2. Maneja las excepciones de Feign Client que indican fallos de red o servicio.
     * Esto es crucial para la orquestación.
     */
    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ErrorResponseDTO> handleFeignStatusException(FeignException ex, WebRequest request) {
        
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String errorType = "Internal Server Error";
        String message = "Fallo en la comunicación con un servicio externo (" + ex.status() + ").";
        
        // Determinar el estado basado en la respuesta de Feign
        if (ex.status() == HttpStatus.NOT_FOUND.value()) {
            status = HttpStatus.NOT_FOUND;
            errorType = "Resource Not Found";
            message = "El recurso solicitado (Usuario o Restaurante) no existe.";
        } else if (ex.status() == HttpStatus.UNAUTHORIZED.value() || ex.status() == HttpStatus.FORBIDDEN.value()) {
            status = HttpStatus.UNAUTHORIZED;
            errorType = "Unauthorized";
            message = "Acceso denegado al servicio externo.";
        } else if (ex.status() == HttpStatus.SERVICE_UNAVAILABLE.value()) {
             // Si el servicio está temporalmente inactivo
            status = HttpStatus.SERVICE_UNAVAILABLE;
            errorType = "Service Unavailable";
            message = "Servicio de Identidad/Catálogo no disponible.";
        }
        
        // Usamos el mensaje del Feign si no es un error genérico
        if (ex.contentUTF8().length() > 0) {
             message = "Error en servicio externo: " + ex.contentUTF8();
        }

        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
            status.value(),
            errorType,
            message,
            request.getDescription(false).replace("uri=", "")
        );
        
        return new ResponseEntity<>(errorResponse, status);
    }
    
    /**
     * 3. Maneja todas las demás excepciones no controladas (RuntimeException, 500s).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleAllExceptions(Exception ex, WebRequest request) {
        
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Unexpected Error",
            "Ocurrió un error interno no esperado: " + ex.getMessage(),
            request.getDescription(false).replace("uri=", "")
        );
        
        // ⚠️ IMPORTANTE: En producción, nunca devuelvas el stack trace completo.
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR); // HTTP 500
    }
}