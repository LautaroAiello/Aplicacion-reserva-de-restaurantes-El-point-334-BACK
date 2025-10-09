package microservice.restaurant_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "configuracion_restaurante")
@Data
@NoArgsConstructor // Necesario para JPA
public class ConfiguracionRestaurante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // BIGSERIAL -> Long

    // Relación OneToOne con RESTAURANTE (Es el lado poseedor de la FK)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurante_id", nullable = false, unique = true)
    @JsonIgnore
    private Restaurante restaurante;

    @Column(name = "tiempo_anticipacion_minutos")
    private Integer tiempoAnticipacionMinutos;

    @Column(name = "min_personas_evento_largo")
    private Integer minPersonasEventoLargo;

    @Column(name = "mapa_ancho", precision = 10, scale = 6)
    private BigDecimal mapaAncho; // Usar BigDecimal para precisión

    @Column(name = "mapa_largo", precision = 10, scale = 6)
    private BigDecimal mapaLargo;

    @Column(name = "mostrar_precios")
    private Boolean mostrarPrecios = true;
}
