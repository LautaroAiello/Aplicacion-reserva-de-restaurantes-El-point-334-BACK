package com.notificacion_service.repository;

import com.notificacion_service.document.NotificacionLog; // Importá tu Documento
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para acceder a la colección 'logs_de_notificaciones' en MongoDB.
 * Spring Data MongoDB implementará automáticamente los métodos CRUD 
 * (save, findById, findAll, delete, etc.).
 */
@Repository
public interface NotificacionLogRepository extends MongoRepository<NotificacionLog, String> {
    
    // Podés agregar métodos de búsqueda personalizados aquí si los necesitás para auditoría.
    // Por ejemplo, para buscar todos los logs de una reserva específica:
    // List<NotificacionLog> findByReservaId(Long reservaId);
}