package microservice.reserva_service.services.dto;

public class ConfiguracionRestauranteDTO {
    private Integer tiempoAnticipacionMinutos;
    private Integer minPersonasEventoLargo;
    
    public ConfiguracionRestauranteDTO(Integer tiempoAnticipacionMinutos, Integer minPersonasEventoLargo) {
        this.tiempoAnticipacionMinutos = tiempoAnticipacionMinutos;
        this.minPersonasEventoLargo = minPersonasEventoLargo;
    }
    
    public Integer getTiempoAnticipacionMinutos() {
        return tiempoAnticipacionMinutos;
    }
    public void setTiempoAnticipacionMinutos(Integer tiempoAnticipacionMinutos) {
        this.tiempoAnticipacionMinutos = tiempoAnticipacionMinutos;
    }
    public Integer getMinPersonasEventoLargo() {
        return minPersonasEventoLargo;
    }
    public void setMinPersonasEventoLargo(Integer minPersonasEventoLargo) {
        this.minPersonasEventoLargo = minPersonasEventoLargo;
    }
    
}
