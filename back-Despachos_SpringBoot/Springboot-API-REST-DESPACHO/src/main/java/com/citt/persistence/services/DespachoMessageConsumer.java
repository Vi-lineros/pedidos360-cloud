package com.citt.persistence.services;

import com.citt.config.RabbitMQConfig;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class DespachoMessageConsumer {

    private static final Logger logger = LoggerFactory.getLogger(DespachoMessageConsumer.class);

    @RabbitListener(queues = RabbitMQConfig.QUEUE_TICKETS)
    public void procesarTicket(String payload, Channel channel, Message message) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            logger.info("Recibido Ticket en Despachos: {}", payload);
            if (payload.contains("ERROR")) {
                throw new RuntimeException("Error simulado en Ticket");
            }
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            logger.error("Fallo procesando Ticket, enviando a DLQ: {}", e.getMessage(), e);
            channel.basicNack(deliveryTag, false, false);
        }
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_ALERTAS)
    public void procesarAlerta(String payload, Channel channel, Message message) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            logger.info("Recibida Alerta en Despachos: {}", payload);
            if (payload.contains("ERROR")) {
                throw new RuntimeException("Error simulado en Alerta");
            }
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            logger.error("Fallo procesando Alerta, enviando a DLQ: {}", e.getMessage(), e);
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
