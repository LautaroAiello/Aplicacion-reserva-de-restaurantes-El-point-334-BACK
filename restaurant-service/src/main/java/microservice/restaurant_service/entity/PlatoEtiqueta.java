package microservice.restaurant_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "plato_etiqueta", uniqueConstraints = {
    // Aplica la restricción UNIQUE de la base de datos a nivel JPA
    @UniqueConstraint(columnNames = {"plato_id", "etiqueta_id"}, name = "uq_plato_etiqueta")
})
@Data
@NoArgsConstructor // Necesario para JPA
public class PlatoEtiqueta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // BIGSERIAL -> Long

    // Relación ManyToOne con PLATO
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plato_id", nullable = false)
    private Plato plato;

    // Relación ManyToOne con ETIQUETA
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etiqueta_id", nullable = false)
    private Etiqueta etiqueta;
}
