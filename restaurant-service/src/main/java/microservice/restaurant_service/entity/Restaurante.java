package microservice.restaurant_service.entity;

import java.time.LocalTime;
import java.util.Set;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

@Entity
@Table(name = "restaurante")
@Data
public class Restaurante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 255)
    private String nombre;

    @Column(name = "activo")
    private Boolean activo = true;

    @Column(name = "telefono", length = 20)
    private String telefono;

    // Mapeo del tipo TIME de PostgreSQL a LocalTime de Java
    @Column(name = "horario_apertura")
    private LocalTime horarioApertura;

    @Column(name = "horario_cierre")
    private LocalTime horarioCierre;

    // 1. OneToOne con DIRECCION (FK: direccion_id)
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER) // EAGER para facilitar el acceso a la dirección
    @JoinColumn(name = "direccion_id", referencedColumnName = "id")
    private Direccion direccion;

    // 2. OneToOne con ENTIDAD_FISCAL (FK: entidad_fiscal_id)
    // Se usa la clase EntidadFiscal minimalista, aunque pertenezca conceptualmente a otro microservicio.
    @Column(name = "entidad_fiscal_id")
    private Long entidad_fiscal_id;

    // 3. OneToOne con CONFIGURACION_RESTAURANTE (Mapeada en ConfiguracionRestaurante, con FK a Restaurante)
    @OneToOne(mappedBy = "restaurante", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ConfiguracionRestaurante configuracion;

    // 4. OneToMany con MESA (Mapeada en Mesa, con FK a Restaurante)
    @OneToMany(mappedBy = "restaurante", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private Set<Mesa> mesas;

    // 5. OneToMany con PLATO (Mapeada en Plato, con FK a Restaurante)
    @OneToMany(mappedBy = "restaurante", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private Set<Plato> platos;
}
