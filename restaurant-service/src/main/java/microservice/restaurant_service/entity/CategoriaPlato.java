package microservice.restaurant_service.entity;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Set;

@Entity
@Table(name = "categoria_plato")
@Data
@NoArgsConstructor
public class CategoriaPlato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 100, unique = true)
    private String nombre;

    // Relación OneToMany con PLATO (Mapeada en Plato)
    @OneToMany(mappedBy = "categoriaPlato", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Plato> platos;
}