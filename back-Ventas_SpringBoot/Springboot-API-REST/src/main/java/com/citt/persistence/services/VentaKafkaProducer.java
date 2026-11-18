package com.citt.persistence.services;

import com.citt.config.KafkaConfig;
import com.citt.exceptions.dto.PedidoEventoDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class VentaKafkaProducer {

    @Autowired
    private KafkaTemplate<String, PedidoEventoDTO> kafkaTemplate;

    public void enviarEventoVenta(PedidoEventoDTO evento) {
        log.info("Enviando evento de venta a Kafka: {}", evento);
        kafkaTemplate.send(KafkaConfig.TOPIC_VENTAS, String.valueOf(evento.getIdVenta()), evento);
    }
}