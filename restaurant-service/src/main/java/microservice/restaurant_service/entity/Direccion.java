package microservice.restaurant_service.entity;

import jakarta.persistence.*;

import lombok.Data; // O usar Getters/Setters manualmente

@Entity
@Table(name = "direccion")
@Data // Usando Lombok para simplificar Getters, Setters, etc.
public class Direccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pais", nullable = false, length = 100)
    private String pais;

    @Column(name = "provincia", length = 100)
    private String provincia;

    @Column(name = "ciudad", length = 100)
    private String ciudad;

    @Column(name = "calle", length = 150)
    private String calle;

    @Column(name = "numero", length = 20)
    private String numero;

    @Column(name = "latitud", length = 50)
    private String latitud;

    @Column(name = "longitud", length = 50)
    private String longitud;

    // Nota: La relación OneToOne con Restaurante se mapea en Restaurante
}