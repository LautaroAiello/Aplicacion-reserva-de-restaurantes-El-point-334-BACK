package microservice.restaurant_service.services;

import org.springframework.stereotype.Service;

import microservice.restaurant_service.entity.Etiqueta;
import microservice.restaurant_service.repositories.EtiquetaRepository;

import java.util.List;
import java.util.Optional;

@Service
public class EtiquetaService {

    private final EtiquetaRepository etiquetaRepository;

    public EtiquetaService(EtiquetaRepository etiquetaRepository) {
        this.etiquetaRepository = etiquetaRepository;
    }

    public List<Etiqueta> listarTodas() {
        return etiquetaRepository.findAll();
    }

    public Optional<Etiqueta> buscarPorId(Long id) {
        return etiquetaRepository.findById(id);
    }

    public Etiqueta guardar(Etiqueta etiqueta) {
        // Lógica de negocio: Comprobar si ya existe una etiqueta con el mismo nombre
        if (etiquetaRepository.findByNombreIgnoreCase(etiqueta.getNombre()).isPresent()) {
            throw new IllegalArgumentException("Ya existe una etiqueta con el nombre: " + etiqueta.getNombre());
        }
        return etiquetaRepository.save(etiqueta);
    }

    public Etiqueta actualizar(Long id, Etiqueta detalles) {
        return etiquetaRepository.findById(id)
            .map(etiqueta -> {
                // Validación para evitar duplicar el nombre al actualizar
                if (!etiqueta.getNombre().equalsIgnoreCase(detalles.getNombre())) {
                    if (etiquetaRepository.findByNombreIgnoreCase(detalles.getNombre()).isPresent()) {
                         throw new IllegalArgumentException("El nuevo nombre de la etiqueta ya existe.");
                    }
                }
                etiqueta.setNombre(detalles.getNombre());
                return etiquetaRepository.save(etiqueta);
            }).orElseThrow(() -> new RuntimeException("Etiqueta no encontrada con ID: " + id));
    }

    public void eliminarPorId(Long id) {
        etiquetaRepository.deleteById(id);
    }
}