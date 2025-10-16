package microservice.reserva_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.TopicExchange; 



@Configuration
public class RabbitMQReservaConfig {
    // Nombre del Exchange: el lugar donde el productor envía el mensaje
    public static final String EXCHANGE_NAME = "reservas.eventos.exchange"; 
    public static final String RESERVATION_ROUTING_KEY = "reserva.hecha";

    @Bean
    public Exchange reservaExchange() {
        // TopicExchange permite routing keys flexibles (e.g., reserva.creada, reserva.cancelada)
        return new TopicExchange(EXCHANGE_NAME);
    }
}