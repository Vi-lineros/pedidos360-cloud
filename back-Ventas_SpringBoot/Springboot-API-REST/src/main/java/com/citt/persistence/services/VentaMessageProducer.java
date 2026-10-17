package com.citt.persistence.services;

import com.citt.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class VentaMessageProducer {

    private static final Logger logger = LoggerFactory.getLogger(VentaMessageProducer.class);

    private final RabbitTemplate rabbitTemplate;

    public VentaMessageProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void enviarNotificacionVenta(String mensaje) {
        logger.info("Enviando mensaje a RabbitMQ: {}", mensaje);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_VENTAS,
                RabbitMQConfig.ROUTING_KEY_NOTIFICACIONES,
                mensaje);
    }
}
