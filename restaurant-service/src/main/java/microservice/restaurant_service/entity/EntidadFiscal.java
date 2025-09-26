package microservice.restaurant_service.entity;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "entidad_fiscal")
@Data
class EntidadFiscal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String cuit;
    private String razonSocial;
    // ... otros campos no relevantes para el mapeo en este servicio
}