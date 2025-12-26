package microservice.restaurant_service.services;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import microservice.restaurant_service.dto.DireccionDTO;
import microservice.restaurant_service.dto.PlatoDTO;
import microservice.restaurant_service.dto.RestauranteDTO;
import microservice.restaurant_service.dto.UsuarioAdminCreationDTO;
import microservice.restaurant_service.dto.UsuarioCreationDTO;
import microservice.restaurant_service.dto.UsuarioDTO;
import microservice.restaurant_service.entity.Direccion;
import microservice.restaurant_service.entity.Favorito;
import microservice.restaurant_service.entity.Plato;
import microservice.restaurant_service.entity.Restaurante;
import microservice.restaurant_service.feign.UsuarioFeign;
import microservice.restaurant_service.repositories.RestauranteRepository;
import microservice.restaurant_service.repositories.FavoritoRepository;
import microservice.restaurant_service.repositories.PlatoRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RestauranteService {

    private final RestauranteRepository restauranteRepository;
    private final UsuarioFeign usuarioFeign;
    private final FavoritoRepository favoritoRepository;
    private final PlatoRepository platoRepository;

    public RestauranteService(RestauranteRepository restauranteRepository, UsuarioFeign usuarioFeign, FavoritoRepository favoritoRepository, PlatoRepository platoRepository) {
        this.restauranteRepository = restauranteRepository;
        this.usuarioFeign = usuarioFeign;
        this.favoritoRepository = favoritoRepository;
        this.platoRepository = platoRepository;
    }

    // 1. Listar todos los restaurantes (DTO)
    public List<RestauranteDTO> listarRestaurantesDTO(Long usuarioId) {
    
    // 1. Traer todos los restaurantes
    List<Restaurante> restaurantes = restauranteRepository.findAll();

    // 2. Obtener los IDs de los restaurantes que ESTE usuario marcó como favoritos
    // Usamos una lista vacía si el usuarioId es null (usuario no logueado)
    List<Long> idsFavoritos = (usuarioId != null) 
        ? favoritoRepository.findRestauranteIdsByUsuarioId(usuarioId) 
        : List.of(); 

    // 3. Mapear y marcar cuáles son favoritos
    return restaurantes.stream()
        .map(r -> {
            RestauranteDTO dto = mapearADTO(r);
            
            // Aquí la magia: Si el ID del restaurante está en la lista de favoritos del usuario -> true
            boolean esFav = idsFavoritos.contains(r.getId());
            dto.setEsFavorito(esFav);
            
            return dto;
        })
        .collect(Collectors.toList());
}

    public RestauranteDTO mapearADTO(Restaurante restaurante) {
        RestauranteDTO dto = new RestauranteDTO();
        dto.setId(restaurante.getId());
        dto.setNombre(restaurante.getNombre());
        dto.setActivo(restaurante.getActivo());
        dto.setTelefono(restaurante.getTelefono());
        dto.setHorarioApertura(restaurante.getHorarioApertura());
        dto.setHorarioCierre(restaurante.getHorarioCierre());
        dto.setImagenUrl(restaurante.getImagenUrl());
        dto.setCantidadReservas(restaurante.getCantidadReservas());
        
        // Mapear Dirección
        if (restaurante.getDireccion() != null) {
            Direccion dirEntidad = restaurante.getDireccion();
            DireccionDTO dirDto = new DireccionDTO();
            
            dirDto.setCalle(dirEntidad.getCalle());
            dirDto.setNumero(dirEntidad.getNumero());
            dirDto.setCiudad(dirEntidad.getCiudad());
            dirDto.setProvincia(dirEntidad.getProvincia());
            dirDto.setPais(dirEntidad.getPais());
            dirDto.setLatitud(dirEntidad.getLatitud());
            dirDto.setLongitud(dirEntidad.getLongitud());

            dto.setDireccion(dirDto);
        }

        // Mapear Configuración
        if (restaurante.getConfiguracion() != null) {
            microservice.restaurant_service.entity.ConfiguracionRestaurante configEntidad = restaurante.getConfiguracion();
            microservice.restaurant_service.dto.ConfiguracionRestauranteDTO configDTO = new microservice.restaurant_service.dto.ConfiguracionRestauranteDTO();
            
            configDTO.setTiempoAnticipacionMinutos(configEntidad.getTiempoAnticipacionMinutos());
            configDTO.setMinPersonasEventoLargo(configEntidad.getMinPersonasEventoLargo());
            configDTO.setMostrarPrecios(configEntidad.getMostrarPrecios());
            dto.setConfiguracion(configDTO);
        }

        List<Plato> platos = platoRepository.findByRestauranteId(restaurante.getId());
        List<PlatoDTO> menuDTO = platos.stream().map(plato -> {
            PlatoDTO pDto = new PlatoDTO();
            pDto.setId(plato.getId());
            pDto.setNombre(plato.getNombre());
            pDto.setDescripcion(plato.getDescripcion());
            pDto.setPrecio(plato.getPrecio());
            pDto.setImagenUrl(plato.getImagenUrl());
            pDto.setEstado(plato.getEstado());
        
            // Mapeo seguro de categoría
            if (plato.getCategoriaPlato() != null) {
                pDto.setNombreCategoria(plato.getCategoriaPlato().getNombre()); // Asumiendo que CategoriaPlato tiene getNombre()
            } else {
                pDto.setNombreCategoria("General");
            }
            return pDto;
        }).collect(Collectors.toList());

        dto.setMenu(menuDTO);
        
        return dto;
    }

    // 2. Obtener un restaurante por ID
    @Transactional
    public Optional<Restaurante> obtenerRestaurantePorId(Long id) {
        return restauranteRepository.findById(id);
    }

    public Optional<RestauranteDTO> obtenerRestauranteDTOPorId(Long id) {
        return restauranteRepository.findById(id).map(this::mapearADTO);
    }

    // 3. Crear un nuevo restaurante
    @Transactional
    public Restaurante crearRestaurante(RestauranteDTO restaurante) {
        
        if (restaurante.getNombre() == null || restaurante.getDireccion() == null) {
             throw new IllegalArgumentException("El nombre y la dirección del restaurante son obligatorios.");
        }
        
        Optional<Restaurante> existente = restauranteRepository.findByNombre(restaurante.getNombre());
        if (existente.isPresent()) {
            throw new RuntimeException("Ya existe un restaurante con el nombre: " + restaurante.getNombre());
        }

        DireccionDTO direccionDTO = restaurante.getDireccion();
        Direccion nuevaDireccion = new Direccion();
        nuevaDireccion.setCalle(direccionDTO.getCalle());
        nuevaDireccion.setNumero(direccionDTO.getNumero());
        nuevaDireccion.setCiudad(direccionDTO.getCiudad());
        nuevaDireccion.setProvincia(direccionDTO.getProvincia());
        nuevaDireccion.setPais(direccionDTO.getPais());
        nuevaDireccion.setLatitud(direccionDTO.getLatitud());
        nuevaDireccion.setLongitud(direccionDTO.getLongitud());

        Restaurante nuevoRestaurante = new Restaurante();
        nuevoRestaurante.setNombre(restaurante.getNombre());
        nuevoRestaurante.setTelefono(restaurante.getTelefono());
        nuevoRestaurante.setHorarioApertura(restaurante.getHorarioApertura());
        nuevoRestaurante.setHorarioCierre(restaurante.getHorarioCierre());
        nuevoRestaurante.setDireccion(nuevaDireccion);
        nuevoRestaurante.setEntidad_fiscal_id(restaurante.getEntidad_fiscal_id());
        nuevoRestaurante.setImagenUrl(restaurante.getImagenUrl()); // Guardar imagen

        if (restaurante.getActivo() == null) {
            nuevoRestaurante.setActivo(true);
        }

        Restaurante restauranteGuardado = restauranteRepository.save(nuevoRestaurante);
        Long restauranteId = restauranteGuardado.getId();

        // SAGA: Llamar a Auth Service
        UsuarioAdminCreationDTO usuarioData = new UsuarioAdminCreationDTO();
        usuarioData.setNombre(restaurante.getNombreUsuario());
        usuarioData.setApellido(restaurante.getApellidoUsuario());
        usuarioData.setEmail(restaurante.getEmailUsuario());
        usuarioData.setPassword(restaurante.getPasswordUsuario());
        usuarioData.setTelefono(restaurante.getTelefonoUsuario());
        usuarioData.setRestauranteId(restauranteId);
        usuarioData.setRol("ADMIN");

        try {
            usuarioFeign.crearUsuarioYAsignarRol(usuarioData);
        } catch (Exception e) { 
            restauranteRepository.delete(restauranteGuardado); 
            throw new IllegalArgumentException(
                "La creación del Restaurante fue cancelada. Fallo al crear el Administrador: " + e.getMessage()
            );
        }

        return restauranteGuardado;
    }

    // 4. Actualizar un restaurante existente (CORREGIDO Y OPTIMIZADO)
    @Transactional
    public Restaurante actualizarRestaurante(Long id, RestauranteDTO detallesDTO) {
        
        Restaurante restaurante = restauranteRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Restaurante no encontrado con ID: " + id));

        // Actualización de campos simples
        restaurante.setNombre(detallesDTO.getNombre());
        restaurante.setActivo(detallesDTO.getActivo());
        restaurante.setTelefono(detallesDTO.getTelefono());
        restaurante.setHorarioApertura(detallesDTO.getHorarioApertura());
        restaurante.setHorarioCierre(detallesDTO.getHorarioCierre());
        
        // Actualizar Imagen
        if (detallesDTO.getImagenUrl() != null) {
            restaurante.setImagenUrl(detallesDTO.getImagenUrl());
        }

        // --- CORRECCIÓN DIRECCIÓN: Actualizar existente en vez de crear nueva ---
        if (detallesDTO.getDireccion() != null) {
            DireccionDTO dirDTO = detallesDTO.getDireccion();
            
            // Recuperamos la dirección que YA EXISTE en la base de datos
            Direccion dir = restaurante.getDireccion();
            
            if (dir == null) {
                // Solo si no existe, creamos una nueva
                dir = new Direccion();
                restaurante.setDireccion(dir);
            }
            
            // Actualizamos los campos SOBRE la instancia existente.
            // Hibernate detectará que el ID es el mismo y hará un UPDATE.
            dir.setCalle(dirDTO.getCalle());
            dir.setNumero(dirDTO.getNumero());
            dir.setCiudad(dirDTO.getCiudad());
            dir.setProvincia(dirDTO.getProvincia());
            dir.setPais(dirDTO.getPais());
            dir.setLatitud(dirDTO.getLatitud());
            dir.setLongitud(dirDTO.getLongitud());
        }

        // --- CORRECCIÓN CONFIGURACIÓN: Actualizar existente ---
        if (detallesDTO.getConfiguracion() != null) {
            microservice.restaurant_service.dto.ConfiguracionRestauranteDTO configDTO = detallesDTO.getConfiguracion();
            
            // Recuperamos la configuración que YA EXISTE
            microservice.restaurant_service.entity.ConfiguracionRestaurante config = restaurante.getConfiguracion();
            
            if (config == null) {
                // Solo si no existe, creamos una nueva y vinculamos
                config = new microservice.restaurant_service.entity.ConfiguracionRestaurante();
                config.setRestaurante(restaurante); // Vincular padre
                restaurante.setConfiguracion(config); // Vincular hijo
            }

            // Actualizamos los campos SOBRE la instancia existente
            config.setTiempoAnticipacionMinutos(configDTO.getTiempoAnticipacionMinutos());
            config.setMinPersonasEventoLargo(configDTO.getMinPersonasEventoLargo());
            config.setMostrarPrecios(configDTO.getMostrarPrecios());
            // Mapear otros campos de configuración si existen en tu DTO/Entidad
        }

        return restauranteRepository.save(restaurante);
    }

    // 5. Eliminar un restaurante
    public void eliminarRestaurante(Long id) {
        restauranteRepository.deleteById(id);
    }

    @Transactional
    public void crearYAsignarGestor(Long restauranteId, UsuarioCreationDTO gestorDTO) {
        if (!restauranteRepository.existsById(restauranteId)) {
            throw new IllegalArgumentException("El Restaurante con ID " + restauranteId + " no existe.");
        }
    
        UsuarioAdminCreationDTO usuarioData = new UsuarioAdminCreationDTO();
        usuarioData.setNombre(gestorDTO.getNombre());
        usuarioData.setApellido(gestorDTO.getApellido());
        usuarioData.setEmail(gestorDTO.getEmail());
        usuarioData.setPassword(gestorDTO.getPassword());
        usuarioData.setTelefono(gestorDTO.getTelefono());
        usuarioData.setRestauranteId(restauranteId);
        usuarioData.setRol("GESTOR");

        try {
            usuarioFeign.crearUsuarioYAsignarRol(usuarioData);
        } catch (Exception e) { 
            throw new IllegalArgumentException(
                "Fallo al crear y asignar Gestor. Causa: " + e.getMessage()
            );
        }
    }

    // --- MÉTODOS DE BÚSQUEDA (CLIENTE) ---

    public List<RestauranteDTO> buscarRestaurantes(String nombre, String etiqueta) {
        // Manejo de nulos para evitar errores en la query
        String nombreFiltro = (nombre == null) ? "" : nombre;
        
        // Llamada al repositorio (asumiendo que tu compañero creó este método en el Repo)
        List<Restaurante> resultados = restauranteRepository.buscarPorNombreYEtiqueta(nombreFiltro, etiqueta);
        
        // Reutilizamos el mapeador que ya tenemos para devolver DTOs
        return resultados.stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
    }

    // Inyectar FavoritoRepository

// 1. TOGGLE FAVORITO (Dar/Quitar Like)
public boolean toggleFavorito(Long restauranteId, Long usuarioId) {
    Optional<Favorito> existente = favoritoRepository.findByUsuarioIdAndRestauranteId(usuarioId, restauranteId);
    
    if (existente.isPresent()) {
        favoritoRepository.delete(existente.get());
        return false; // Ya no es favorito (se quitó)
    } else {
        Restaurante restaurante = restauranteRepository.findById(restauranteId)
            .orElseThrow(() -> new IllegalArgumentException("Restaurante no encontrado"));
            
        Favorito nuevo = new Favorito();
        nuevo.setUsuarioId(usuarioId);
        nuevo.setRestaurante(restaurante);
        favoritoRepository.save(nuevo);
        return true; // Ahora es favorito (se agregó)
    }
}

// 2. MIS FAVORITOS
public List<RestauranteDTO> obtenerMisFavoritos(Long usuarioId) {
    return favoritoRepository.findAllByUsuarioId(usuarioId).stream()
            .map(this::mapearADTO)
            .collect(Collectors.toList());
}

// 3. TOP POPULARES
public List<RestauranteDTO> obtenerMasPopulares(int limite, Long usuarioId) {
        
        // 1. Pedimos al Repo los "limite" (ej. 10) restaurantes con más likes
        // PageRequest.of(0, limite) es como decir "LIMIT 10" en SQL
        List<Restaurante> populares = favoritoRepository.findTopPopulares(PageRequest.of(0, limite));

        // 2. Si hay un usuario logueado, buscamos SUS favoritos para marcar la UI
        List<Long> misFavoritosIds = (usuarioId != null) 
            ? favoritoRepository.findRestauranteIdsByUsuarioId(usuarioId) 
            : List.of();

        // 3. Convertimos a DTO
        return populares.stream()
            .map(r -> {
                RestauranteDTO dto = mapearADTO(r);
                // Si el restaurante popular también es MI favorito, true
                dto.setEsFavorito(misFavoritosIds.contains(r.getId())); 
                return dto;
            })
            .collect(Collectors.toList());
    }

     // Listar gestores: llama al auth-service a través de Feign
    public List<UsuarioDTO> listarGestores(Long restauranteId) {
        // "GESTOR" es el rol que buscamos
        return usuarioFeign.obtenerUsuariosPorRestauranteYRol(restauranteId, "GESTOR");
    }

    // Eliminar gestor: llama al auth-service a través de Feign
    public void eliminarGestor(Long restauranteId, Long usuarioId) {
        usuarioFeign.eliminarGestor(restauranteId, usuarioId);
    }

    @Transactional
    public void incrementarContadorReservas(Long id) {
        Restaurante restaurante = restauranteRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Restaurante no encontrado"));
        
        // Obtenemos el valor actual (o 0 si es nulo)
        Long cantidadActual = restaurante.getCantidadReservas() == null ? 0L : restaurante.getCantidadReservas();
        
        // Sumamos 1
        restaurante.setCantidadReservas(cantidadActual + 1);
        
        // Guardamos
        restauranteRepository.save(restaurante);
    }

}