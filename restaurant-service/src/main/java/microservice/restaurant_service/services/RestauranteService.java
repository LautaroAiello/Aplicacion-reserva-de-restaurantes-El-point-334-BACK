package microservice.restaurant_service.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import microservice.restaurant_service.dto.DireccionDTO;
import microservice.restaurant_service.dto.RestauranteDTO;
import microservice.restaurant_service.dto.UsuarioAdminCreationDTO;
import microservice.restaurant_service.dto.UsuarioCreationDTO;
import microservice.restaurant_service.entity.Direccion;
import microservice.restaurant_service.entity.Restaurante;
import microservice.restaurant_service.feign.UsuarioFeign;
import microservice.restaurant_service.repositories.RestauranteRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RestauranteService {

    private final RestauranteRepository restauranteRepository;
    private final UsuarioFeign usuarioFeign;

    public RestauranteService(RestauranteRepository restauranteRepository, UsuarioFeign usuarioFeign) {
        this.restauranteRepository = restauranteRepository;
        this.usuarioFeign = usuarioFeign;
    }

    // 1. Listar todos los restaurantes (DTO)
    public List<RestauranteDTO> listarRestaurantesDTO() {
        List<Restaurante> restaurantes = restauranteRepository.findAll();
        return restaurantes.stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
    }

    private RestauranteDTO mapearADTO(Restaurante restaurante) {
        RestauranteDTO dto = new RestauranteDTO();
        dto.setId(restaurante.getId());
        dto.setNombre(restaurante.getNombre());
        dto.setActivo(restaurante.getActivo());
        dto.setTelefono(restaurante.getTelefono());
        dto.setHorarioApertura(restaurante.getHorarioApertura());
        dto.setHorarioCierre(restaurante.getHorarioCierre());
        dto.setImagenUrl(restaurante.getImagenUrl());
        
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
            // Mapear otros campos si existen en el DTO
            
            dto.setConfiguracion(configDTO);
        }
        
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
}