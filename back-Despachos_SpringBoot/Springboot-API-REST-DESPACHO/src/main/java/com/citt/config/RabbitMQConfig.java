package com.citt.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_DESPACHOS = "despachos.direct.exchange";
    public static final String DLX_DESPACHOS = "despachos.dlx.exchange";

    public static final String QUEUE_TICKETS = "despachos.tickets.queue";
    public static final String ROUTING_KEY_TICKETS = "tickets.key";
    public static final String DLQ_TICKETS = "despachos.tickets.dlq";

    public static final String QUEUE_ALERTAS = "despachos.alertas.queue";
    public static final String ROUTING_KEY_ALERTAS = "alertas.key";
    public static final String DLQ_ALERTAS = "despachos.alertas.dlq";

    @Bean
    public DirectExchange despachosExchange() {
        return new DirectExchange(EXCHANGE_DESPACHOS);
    }

    @Bean
    public DirectExchange despachosDlx() {
        return new DirectExchange(DLX_DESPACHOS);
    }

    @Bean
    public Queue ticketsQueue() {
        return QueueBuilder.durable(QUEUE_TICKETS)
                .withArgument("x-dead-letter-exchange", DLX_DESPACHOS)
                .withArgument("x-dead-letter-routing-key", QUEUE_TICKETS + ".dlq")
                .build();
    }

    @Bean
    public Queue ticketsDlq() {
        return QueueBuilder.durable(DLQ_TICKETS).build();
    }

    @Bean
    public Queue alertasQueue() {
        return QueueBuilder.durable(QUEUE_ALERTAS)
                .withArgument("x-dead-letter-exchange", DLX_DESPACHOS)
                .withArgument("x-dead-letter-routing-key", QUEUE_ALERTAS + ".dlq")
                .build();
    }

    @Bean
    public Queue alertasDlq() {
        return QueueBuilder.durable(DLQ_ALERTAS).build();
    }

    @Bean
    public Binding bindingTickets() {
        return BindingBuilder.bind(ticketsQueue())
                .to(despachosExchange())
                .with(ROUTING_KEY_TICKETS);
    }

    @Bean
    public Binding bindingTicketsDlq() {
        return BindingBuilder.bind(ticketsDlq())
                .to(despachosDlx())
                .with(QUEUE_TICKETS + ".dlq");
    }

    @Bean
    public Binding bindingAlertas() {
        return BindingBuilder.bind(alertasQueue())
                .to(despachosExchange())
                .with(ROUTING_KEY_ALERTAS);
    }

    @Bean
    public Binding bindingAlertasDlq() {
        return BindingBuilder.bind(alertasDlq())
                .to(despachosDlx())
                .with(QUEUE_ALERTAS + ".dlq");
    }
}
