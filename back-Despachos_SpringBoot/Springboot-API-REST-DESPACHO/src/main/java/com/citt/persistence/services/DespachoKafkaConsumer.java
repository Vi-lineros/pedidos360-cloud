package com.citt.persistence.services;

import com.citt.exceptions.dto.PedidoEventoDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@Service
public class DespachoKafkaConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(DespachoKafkaConsumer.class);

    private final DespachoService despachoService;

    public DespachoKafkaConsumer(DespachoService despachoService) {
        this.despachoService = despachoService;
    }

    @KafkaListener(
            topics = "${app.kafka.topics.ventas}",
            groupId = "${spring.kafka.consumer.group-id}")
    public void consumirEventoVenta(
            PedidoEventoDTO evento,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        LOGGER.info("Evento Kafka recibido: topic={}, partition={}, offset={}, payload={}",
                topic, partition, offset, evento);
        if (evento == null || evento.getIdVenta() == null) {
            LOGGER.error("Evento de venta inválido: topic={}, partition={}, offset={}, payload={}",
                    topic, partition, offset, evento);
            throw new IllegalArgumentException("El evento de venta debe incluir el ID de la venta");
        }

        despachoService.procesarEventoVenta(evento);
    }
}