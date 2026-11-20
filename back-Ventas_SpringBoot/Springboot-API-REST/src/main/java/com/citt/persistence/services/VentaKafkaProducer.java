package com.citt.persistence.services;

import com.citt.exceptions.dto.PedidoEventoDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class VentaKafkaProducer {

    @Value("${app.kafka.topics.ventas}")
    private String topic;

    @Autowired
    private KafkaTemplate<String, PedidoEventoDTO> kafkaTemplate;

    public void enviarEventoVenta(PedidoEventoDTO evento) {
        if (evento == null) {
            throw new IllegalArgumentException("El evento de venta no puede ser nulo");
        }
        if (evento.getIdVenta() == null) {
            throw new IllegalArgumentException("El evento de venta debe incluir el ID de la venta");
        }

        String key = evento.getIdVenta().toString();
        log.info("Enviando evento Kafka: topic={}, ventaId={}", topic, key);
        kafkaTemplate.send(topic, key, evento).whenComplete((result, error) -> {
            if (error != null) {
                log.error("Error enviando evento Kafka: topic={}, ventaId={}", topic, key, error);
                return;
            }

            var metadata = result.getRecordMetadata();
            log.info("Evento Kafka enviado: topic={}, partition={}, offset={}",
                    metadata.topic(), metadata.partition(), metadata.offset());
        });
    }
}