package microservice.restaurant_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "favorito", uniqueConstraints = {
    // Un usuario no puede darle like dos veces al mismo restaurante
    @UniqueConstraint(columnNames = {"usuario_id", "restaurante_id"})
})
@Data
@NoArgsConstructor
public class Favorito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId; // Referencia lógica al USUARIO-SERVICE

    // Relación con Restaurante para poder hacer conteos fáciles
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurante_id", nullable = false)
    private Restaurante restaurante;

    private LocalDateTime fechaCreacion;

    @PrePersist
    public void prePersist() {
        this.fechaCreacion = LocalDateTime.now();
    }
}