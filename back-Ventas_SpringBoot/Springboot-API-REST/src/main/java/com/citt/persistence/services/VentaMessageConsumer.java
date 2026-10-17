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
public class VentaMessageConsumer {

    private static final Logger logger = LoggerFactory.getLogger(VentaMessageConsumer.class);

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NOTIFICACIONES)
    public void recibirNotificacion(String payload, Channel channel, Message message) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            logger.info("Mensaje recibido en Ventas: {}", payload);

            if (payload.contains("ERROR")) {
                throw new RuntimeException("Fallo simulado procesando la notificación");
            }

            channel.basicAck(deliveryTag, false);
            logger.info("Mensaje procesado y confirmado (ACK).");
        } catch (Exception e) {
            logger.error("Error procesando mensaje: {}. Enviando a DLQ...", e.getMessage(), e);
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
