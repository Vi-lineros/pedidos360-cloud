package com.citt.persistence.services;

import com.citt.config.KafkaConfig;
import com.citt.exceptions.dto.PedidoEventoDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DespachoKafkaConsumer {

    @KafkaListener(topics = KafkaConfig.TOPIC_VENTAS, groupId = "despachos-group")
    public void consumirEventoVenta(PedidoEventoDTO evento) {
        log.info("¡Evento de venta recibido desde Kafka en Despachos!: {}", evento);
        
    }
}