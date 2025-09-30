package microservice.restaurant_service.services;

import org.springframework.stereotype.Service;

import microservice.restaurant_service.entity.Etiqueta;
import microservice.restaurant_service.entity.Plato;
import microservice.restaurant_service.entity.PlatoEtiqueta;
import microservice.restaurant_service.repositories.PlatoEtiquetaRepository;

import java.util.List;

@Service
public class PlatoEtiquetaService {

    private final PlatoEtiquetaRepository platoEtiquetaRepository;
    private final PlatoService platoService; // Dependencia para validar que el plato exista
    private final EtiquetaService etiquetaService; // Dependencia para validar que la etiqueta exista

    public PlatoEtiquetaService(PlatoEtiquetaRepository platoEtiquetaRepository, PlatoService platoService, EtiquetaService etiquetaService) {
        this.platoEtiquetaRepository = platoEtiquetaRepository;
        this.platoService = platoService;
        this.etiquetaService = etiquetaService;
    }

    // Método principal: ASOCIAR una etiqueta a un plato
    public PlatoEtiqueta asociarEtiquetaAPlato(Long platoId, Long etiquetaId) {
        // 1. Validar existencia del Plato
        Plato plato = platoService.buscarPorId(platoId)
            .orElseThrow(() -> new RuntimeException("Plato no encontrado con ID: " + platoId));

        // 2. Validar existencia de la Etiqueta
        Etiqueta etiqueta = etiquetaService.buscarPorId(etiquetaId)
            .orElseThrow(() -> new RuntimeException("Etiqueta no encontrada con ID: " + etiquetaId));

        // 3. Verificar si la asociación ya existe
        if (platoEtiquetaRepository.findByPlatoIdAndEtiquetaId(platoId, etiquetaId).isPresent()) {
            throw new IllegalArgumentException("La etiqueta ya está asociada a este plato.");
        }

        // 4. Crear y guardar la asociación
        PlatoEtiqueta nuevaAsociacion = new PlatoEtiqueta();
        nuevaAsociacion.setPlato(plato);
        nuevaAsociacion.setEtiqueta(etiqueta);

        return platoEtiquetaRepository.save(nuevaAsociacion);
    }
    
    // Método principal: DESASOCIAR una etiqueta de un plato
    public void desasociarEtiquetaDePlato(Long platoId, Long etiquetaId) {
        PlatoEtiqueta asociacion = platoEtiquetaRepository.findByPlatoIdAndEtiquetaId(platoId, etiquetaId)
            .orElseThrow(() -> new RuntimeException("Asociación no encontrada entre Plato ID: " + platoId + " y Etiqueta ID: " + etiquetaId));
        
        platoEtiquetaRepository.delete(asociacion);
    }
    
    // Listar las asociaciones de un plato (para ver sus etiquetas)
    public List<PlatoEtiqueta> listarAsociacionesPorPlato(Long platoId) {
        return platoEtiquetaRepository.findByPlatoId(platoId);
    }
}