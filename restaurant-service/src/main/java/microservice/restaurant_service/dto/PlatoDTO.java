package microservice.restaurant_service.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PlatoDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private String imagenUrl;
    private String estado; // 'DISPONIBLE', 'AGOTADO'
    
    // En lugar del objeto entero, mandamos solo el nombre de la categoría
    private String nombreCategoria; 
}
