package microservice.restaurant_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "plato")
@Data
@NoArgsConstructor
public class Plato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 1. Relación ManyToOne con RESTAURANTE
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurante_id", nullable = false)
    @JsonIgnore
    private Restaurante restaurante;

    // 2. Relación ManyToOne con CATEGORIA_PLATO
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_plato_id")
    @JsonIgnoreProperties({"platos", "hibernateLazyInitializer", "handler"})
    private CategoriaPlato categoriaPlato;

    @Column(name = "nombre", nullable = false, length = 255)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    // Mapeo para el tipo DECIMAL(10,2) de PostgreSQL
    @Column(name = "precio", precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(name = "estado", length = 50)
    private String estado; // Ej: 'DISPONIBLE', 'AGOTADO'

    @Column(name = "imagen_url", length = 500)
    private String imagenUrl;

}
// 3. Relación ManyToMany con ETIQUETA a través de PLATO_ETIQUETA (Opcional por ahora)
// Para simplificar, nos enfocaremos en las relaciones de un solo lado