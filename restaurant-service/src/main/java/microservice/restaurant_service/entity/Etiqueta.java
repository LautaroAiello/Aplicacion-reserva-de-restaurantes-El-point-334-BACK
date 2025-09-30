package microservice.restaurant_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "etiqueta")
@Data
@NoArgsConstructor // Necesario para JPA
public class Etiqueta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // BIGSERIAL -> Long

    @Column(name = "nombre", nullable = false, length = 100, unique = true)
    private String nombre;
    
    // Opcional: Relación bidireccional con PLATO_ETIQUETA si la necesitas para consultas
    // @OneToMany(mappedBy = "etiqueta", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // private Set<PlatoEtiqueta> platosAsociados;
}
