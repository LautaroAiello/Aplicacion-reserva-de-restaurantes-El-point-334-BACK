package microservice.reserva_service.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "reserva_mesa", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"reserva_id", "mesa_id"}) // Mapea la restricción UNIQUE del SQL
})
@Getter @Setter
public class ReservaMesa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reserva_id", nullable = false) // FK a la tabla 'reserva'
    @JsonIgnore
    private Reserva reserva;

    @Column(name = "mesa_id", nullable = false)
    private Long mesaId; // FK lógica a restaurant-service (Mesa)

    // --- GETTERS Y SETTERS ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Reserva getReserva() {
        return reserva;
    }

    public void setReserva(Reserva reserva) {
        this.reserva = reserva;
    }

    public Long getMesaId() {
        return mesaId;
    }

    public void setMesaId(Long mesaId) {
        this.mesaId = mesaId;
    }
}
