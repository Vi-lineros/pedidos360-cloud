package com.citt.persistence.services;

import com.citt.exceptions.dto.PedidoEventoDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class DespachoKafkaProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(DespachoKafkaProducer.class);

    private final KafkaTemplate<String, PedidoEventoDTO> kafkaTemplate;
    private final String topic;

    public DespachoKafkaProducer(
            KafkaTemplate<String, PedidoEventoDTO> kafkaTemplate,
            @Value("${app.kafka.topics.despachos}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void enviarEventoDespacho(PedidoEventoDTO evento) {
        if (evento == null) {
            throw new IllegalArgumentException("El evento de despacho no puede ser nulo");
        }
        if (evento.getIdVenta() == null) {
            throw new IllegalArgumentException("El evento de despacho debe incluir el ID de la venta");
        }

        String key = evento.getIdVenta().toString();
        LOGGER.info("Enviando evento Kafka: topic={}, ventaId={}", topic, key);
        kafkaTemplate.send(topic, key, evento).whenComplete((result, error) -> {
            if (error != null) {
                LOGGER.error("Error enviando evento Kafka: topic={}, ventaId={}", topic, key, error);
                return;
            }

            var metadata = result.getRecordMetadata();
            LOGGER.info("Evento Kafka enviado: topic={}, partition={}, offset={}",
                    metadata.topic(), metadata.partition(), metadata.offset());
        });
    }
}
