package microservice.restaurant_service.services;


import org.springframework.stereotype.Service;

import microservice.restaurant_service.entity.CategoriaPlato;
import microservice.restaurant_service.repositories.CategoriaPlatoRepository;

import java.util.List;
import java.util.Optional;

@Service
public class CategoriaPlatoService {

    private final CategoriaPlatoRepository categoriaPlatoRepository;

    public CategoriaPlatoService(CategoriaPlatoRepository categoriaPlatoRepository) {
        this.categoriaPlatoRepository = categoriaPlatoRepository;
    }

    public List<CategoriaPlato> listarTodas() {
        return categoriaPlatoRepository.findAll();
    }

    public Optional<CategoriaPlato> buscarPorId(Long id) {
        return categoriaPlatoRepository.findById(id);
    }

    public CategoriaPlato guardar(CategoriaPlato categoria) {
        // Lógica de negocio: evitar categorías duplicadas
        if (categoriaPlatoRepository.findByNombreIgnoreCase(categoria.getNombre()).isPresent()) {
            throw new IllegalArgumentException("Ya existe una categoría con el nombre: " + categoria.getNombre());
        }
        return categoriaPlatoRepository.save(categoria);
    }

    public void eliminarPorId(Long id) {
        categoriaPlatoRepository.deleteById(id);
    }
}