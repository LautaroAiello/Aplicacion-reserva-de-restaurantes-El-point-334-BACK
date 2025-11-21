package microservice.restaurant_service.services;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import microservice.restaurant_service.dto.ConfiguracionRestauranteDTO;
import microservice.restaurant_service.dto.DireccionDTO;
import microservice.restaurant_service.dto.RestauranteDTO;
import microservice.restaurant_service.dto.UsuarioAdminCreationDTO;
import microservice.restaurant_service.dto.UsuarioCreationDTO;
import microservice.restaurant_service.entity.ConfiguracionRestaurante;
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

    //@Autowired
    public RestauranteService(RestauranteRepository restauranteRepository, UsuarioFeign usuarioFeign) {
        this.restauranteRepository = restauranteRepository;
        this.usuarioFeign = usuarioFeign;
    }

    // 1. Obtener todos los restaurantes
    // public List<Restaurante> listarTodos() {
    //     return restauranteRepository.findAll();
    // }
    public List<RestauranteDTO> listarRestaurantesDTO() {
    List<Restaurante> restaurantes = restauranteRepository.findAll();

    // Mapear la lista de Entidades a lista de DTOs
    return restaurantes.stream()
        .map(this::mapearADTO) // Usamos un método auxiliar para no repetir código
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
    
    return dto;
}

    // 2. Obtener un restaurante por ID
    @Transactional
    public Optional<Restaurante> obtenerRestaurantePorId(Long id) {
        return restauranteRepository.findById(id);
    }

    public Optional<RestauranteDTO> obtenerRestauranteDTOPorId(Long id) {
    return restauranteRepository.findById(id)
        .map(restaurante -> {
            // 🔑 Mapeo ocurre en el Service (o en una clase Mapper inyectada)
            RestauranteDTO dto = new RestauranteDTO();
                    dto.setId(restaurante.getId());
                    dto.setNombre(restaurante.getNombre());
                    dto.setActivo(restaurante.getActivo());
                    dto.setTelefono(restaurante.getTelefono());
                    dto.setHorarioApertura(restaurante.getHorarioApertura());
                    dto.setHorarioCierre(restaurante.getHorarioCierre());
                    dto.setEntidad_fiscal_id(restaurante.getEntidad_fiscal_id());
                    dto.setImagenUrl(restaurante.getImagenUrl());
                    if (restaurante.getDireccion() != null) {
                        Direccion dirEntidad = restaurante.getDireccion();
                        DireccionDTO dirDto = new DireccionDTO();

                        dirDto.setCalle(dirEntidad.getCalle());
                     dirDto.setNumero(dirEntidad.getNumero());
                        dirDto.setCiudad(dirEntidad.getCiudad());
                        dirDto.setProvincia(dirEntidad.getProvincia());
                        dirDto.setPais(dirEntidad.getPais());
                        // Si usas lat/long en el DTO:
                        dirDto.setLatitud(dirEntidad.getLatitud());
                        dirDto.setLongitud(dirEntidad.getLongitud());

                        dto.setDireccion(dirDto);
                    }
                    return dto;
            });
}


    // 3. Crear un nuevo restaurante
    @Transactional
    public Restaurante crearRestaurante(RestauranteDTO restaurante) {
        
        // --- 1. VALIDACIÓN BÁSICA Y NEGOCIO ---
        
        if (restaurante.getNombre() == null || restaurante.getDireccion() == null) {
             throw new IllegalArgumentException("El nombre y la dirección del restaurante son obligatorios.");
        }
        
        // Verificar unicidad del nombre
        Optional<Restaurante> existente = restauranteRepository.findByNombre(restaurante.getNombre());
        if (existente.isPresent()) {
            throw new RuntimeException("Ya existe un restaurante con el nombre: " + restaurante.getNombre());
        }

        // Mapear y crear la entidad Direccion
        DireccionDTO direccionDTO = restaurante.getDireccion();
        Direccion nuevaDireccion = new Direccion();
        nuevaDireccion.setCalle(direccionDTO.getCalle());
        nuevaDireccion.setNumero(direccionDTO.getNumero());
        nuevaDireccion.setCiudad(direccionDTO.getCiudad());
        nuevaDireccion.setProvincia(direccionDTO.getProvincia());
        nuevaDireccion.setPais(direccionDTO.getPais());
        nuevaDireccion.setLatitud(direccionDTO.getLatitud());
        nuevaDireccion.setLongitud(direccionDTO.getLongitud());

        // Mapear la entidad Restaurante
        Restaurante nuevoRestaurante = new Restaurante();
        nuevoRestaurante.setNombre(restaurante.getNombre());
        nuevoRestaurante.setTelefono(restaurante.getTelefono());
        nuevoRestaurante.setHorarioApertura(restaurante.getHorarioApertura());
        nuevoRestaurante.setHorarioCierre(restaurante.getHorarioCierre());
        nuevoRestaurante.setDireccion(nuevaDireccion); // Asignar la dirección mapeada
        nuevoRestaurante.setEntidad_fiscal_id(restaurante.getEntidad_fiscal_id());

        if (restaurante.getActivo() == null) {
            restaurante.setActivo(true);
        }


        // Guardar el restaurante (persistencia local)
        Restaurante restauranteGuardado = restauranteRepository.save(nuevoRestaurante);
        Long restauranteId = restauranteGuardado.getId(); // ID generado que se usará en el Feign

        // Mapear el DTO para el Feign
        UsuarioAdminCreationDTO usuarioData = new UsuarioAdminCreationDTO();
        usuarioData.setNombre(restaurante.getNombreUsuario());
        usuarioData.setApellido(restaurante.getApellidoUsuario());
        usuarioData.setEmail(restaurante.getEmailUsuario());
        usuarioData.setPassword(restaurante.getPasswordUsuario());
        usuarioData.setTelefono(restaurante.getTelefonoUsuario());

        // Agregar los datos de asignación de rol que el USUARIO-SERVICE necesita
        usuarioData.setRestauranteId(restauranteId);
        usuarioData.setRol("ADMIN");

        try {
            // Llama a Feign. El USUARIO-SERVICE creará el Usuario y el registro en UsuarioRestaurante.
            usuarioFeign.crearUsuarioYAsignarRol(usuarioData);
        } catch (Exception e) { 
            // ⚠️ Compensación: Si Feign falla (ej. Email duplicado en el USUARIO-SERVICE, error de conexión, etc.),
            // el Restaurante ya se creó (Paso 1). Para la atomicidad de la SAGA, revertimos la creación:
        
            // Eliminamos el restaurante y la dirección asociada (gracias al CascadeType.ALL)
            restauranteRepository.delete(restauranteGuardado); 
        
            // Lanzamos una excepción que será capturada por el Controller
            throw new IllegalArgumentException(
                "La creación del Restaurante fue cancelada. Fallo al crear el Administrador en el Servicio de Usuarios: " + e.getMessage()
            );
        }

        return restauranteGuardado;
    }

    // 4. Actualizar un restaurante existente
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
    // @Transactional
    // public Restaurante actualizarRestaurante(Long id, RestauranteDTO detallesDTO) {
    //     return restauranteRepository.findById(id)
    //         .map(restaurante -> {
    //             // Actualización de campos
    //             restaurante.setNombre(detalles.getNombre());
    //             restaurante.setActivo(detalles.getActivo());
    //             restaurante.setTelefono(detalles.getTelefono());
    //             restaurante.setHorarioApertura(detalles.getHorarioApertura());
    //             restaurante.setHorarioCierre(detalles.getHorarioCierre());

    //             // Al ser el dueño de la FK, al guardar el Restaurante,
    //             // JPA maneja automáticamente la actualización de Direccion.
    //             if (detalles.getDireccion() != null) {
    //                 restaurante.setDireccion(detalles.getDireccion());
    //             }

    //             return restauranteRepository.save(restaurante);
    //         }).orElseThrow(() -> new RuntimeException("Restaurante no encontrado con ID: " + id)); // Manejo de excepciones
    // }

    // 5. Eliminar un restaurante
    public void eliminarRestaurante(Long id) {
        restauranteRepository.deleteById(id);
    }

    @Transactional
    public void crearYAsignarGestor(Long restauranteId, UsuarioCreationDTO gestorDTO) {

        // --- 1. Validación Local: El Restaurante debe existir ---
        if (!restauranteRepository.existsById(restauranteId)) {
            throw new IllegalArgumentException("El Restaurante con ID " + restauranteId + " no existe.");
        }
    
        // --- 2. Preparar DTO para Feign ---
        // Mapeamos los campos del GestorDTO a nuestro DTO de Feign (UsuarioAdminCreationDTO)
        UsuarioAdminCreationDTO usuarioData = new UsuarioAdminCreationDTO();
        usuarioData.setNombre(gestorDTO.getNombre());
        usuarioData.setApellido(gestorDTO.getApellido());
        usuarioData.setEmail(gestorDTO.getEmail());
        usuarioData.setPassword(gestorDTO.getPassword());
        usuarioData.setTelefono(gestorDTO.getTelefono());
    
        // 💡 Datos de Asignación de Rol
        usuarioData.setRestauranteId(restauranteId);
        usuarioData.setRol("GESTOR"); // El USUARIO-SERVICE aplicará la restricción 1:1

        // --- 3. Llamada Remota (SAGA) ---
        try {
            // Llama a Feign. El USUARIO-SERVICE creará el Usuario, validará la restricción 1:1
            // y creará el registro en UsuarioRestaurante.
            usuarioFeign.crearUsuarioYAsignarRol(usuarioData);
            
        } catch (Exception e) { 
            // Capturar errores de Feign (ej. email duplicado, fallo en la restricción 1:1 del Gestor)
            throw new IllegalArgumentException(
                "Fallo al crear y asignar Gestor. El proceso fue cancelado. Causa: " + e.getMessage()
            );
        }
    
        // ⚠️ Nota: No hay compensación aquí porque no se guardó nada en el CATALOGO-SERVICE.
        // Si falla, simplemente se lanza la excepción.
    }
    
}