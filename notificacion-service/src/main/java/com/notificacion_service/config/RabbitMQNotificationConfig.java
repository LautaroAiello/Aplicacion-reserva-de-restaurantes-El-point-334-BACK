package com.notificacion_service.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQNotificationConfig {
    public static final String EXCHANGE_NAME = "reservas.eventos.exchange";
    public static final String NOTIFICATION_QUEUE = "reservas.notificacion.reserva";
    public static final String RESERVATION_ROUTING_KEY = "reserva.hecha";

    @Bean
    public Exchange reservaExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue notificationQueue() {
        // Crea la cola si no existe. 'true' indica que es durable (persiste si RabbitMQ cae)
        return new Queue(NOTIFICATION_QUEUE, true); 
    }

    @Bean
    public Binding binding(Queue notificationQueue, Exchange reservaExchange) {
        // Conecta la cola al exchange usando el routing key (solo recibe mensajes con esa llave)
        return BindingBuilder.bind(notificationQueue)
                             .to(reservaExchange)
                             .with(RESERVATION_ROUTING_KEY)
                             .noargs();
    }
}