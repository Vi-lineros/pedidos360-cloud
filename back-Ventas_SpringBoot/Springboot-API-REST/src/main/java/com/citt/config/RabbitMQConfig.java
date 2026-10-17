package com.citt.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_VENTAS = "ventas.topic.exchange";
    public static final String QUEUE_NOTIFICACIONES = "ventas.notificaciones.queue";
    public static final String ROUTING_KEY_NOTIFICACIONES = "ventas.notificaciones.routing.key";
    public static final String DLQ_NOTIFICACIONES = "ventas.notificaciones.dlq";
    public static final String DLX_VENTAS = "ventas.dlx.exchange";

    @Bean
    public TopicExchange ventasExchange() {
        return new TopicExchange(EXCHANGE_VENTAS);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX_VENTAS);
    }

    @Bean
    public Queue notificacionesQueue() {
        return QueueBuilder.durable(QUEUE_NOTIFICACIONES)
                .withArgument("x-dead-letter-exchange", DLX_VENTAS)
                .withArgument("x-dead-letter-routing-key", QUEUE_NOTIFICACIONES + ".dlq")
                .build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DLQ_NOTIFICACIONES).build();
    }

    @Bean
    public Binding bindingNotificaciones() {
        return BindingBuilder.bind(notificacionesQueue())
                .to(ventasExchange())
                .with(ROUTING_KEY_NOTIFICACIONES);
    }

    @Bean
    public Binding bindingDLQ() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with(QUEUE_NOTIFICACIONES + ".dlq");
    }
}
