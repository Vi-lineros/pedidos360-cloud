package com.citt.persistence.services;

import com.citt.exceptions.dto.PedidoEventoDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@Service
public class VentaKafkaConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(VentaKafkaConsumer.class);

    private final VentaService ventaService;

    public VentaKafkaConsumer(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    @KafkaListener(
            topics = "${app.kafka.topics.despachos}",
            groupId = "${spring.kafka.consumer.group-id}")
    public void consumirEventoDespacho(
            PedidoEventoDTO evento,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        LOGGER.info("Evento Kafka recibido: topic={}, partition={}, offset={}, payload={}",
                topic, partition, offset, evento);
        if (evento == null || evento.getIdVenta() == null) {
            LOGGER.error("Evento de despacho inválido: topic={}, partition={}, offset={}, payload={}",
                    topic, partition, offset, evento);
            throw new IllegalArgumentException("El evento de despacho debe incluir el ID de la venta");
        }

        ventaService.procesarEventoDespacho(evento);
    }
}
