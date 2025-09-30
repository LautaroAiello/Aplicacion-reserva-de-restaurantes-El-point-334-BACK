package microservice.restaurant_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "etiqueta_filtrado_restaurante", uniqueConstraints = {
    // Aplica la restricción UNIQUE para evitar duplicar la misma etiqueta en el mismo restaurante
    @UniqueConstraint(columnNames = {"restaurante_id", "etiqueta_id"}, name = "uq_restaurante_etiqueta")
})
@Data
@NoArgsConstructor // Necesario para JPA
public class EtiquetaFiltradoRestaurante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // BIGSERIAL -> Long

    // Relación ManyToOne con RESTAURANTE
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurante_id", nullable = false)
    private Restaurante restaurante;

    // Relación ManyToOne con ETIQUETA
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etiqueta_id", nullable = false)
    private Etiqueta etiqueta;
}
