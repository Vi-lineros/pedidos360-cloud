package com.citt.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@EnableKafka
public class KafkaConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaConfig.class);

    public static final String TOPIC_VENTAS = "ventas-eventos";
    public static final String TOPIC_DESPACHOS = "despachos-eventos";

    @Value("${app.kafka.topics.ventas}")
    private String ventasTopicName;

    @Value("${app.kafka.topics.despachos}")
    private String despachosTopicName;

    @Bean
    public NewTopic topicoEventosVentas() {
        return TopicBuilder.name(ventasTopicName)
                .partitions(3)
                .replicas(3)
                .config(TopicConfig.RETENTION_MS_CONFIG, "604800000")
                .config(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_DELETE)
                .config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, "2")
                .build();
    }

    @Bean
    public NewTopic topicoEventosDespachos() {
        return TopicBuilder.name(despachosTopicName)
                .partitions(3)
                .replicas(3)
                .config(TopicConfig.RETENTION_MS_CONFIG, "604800000")
                .config(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_DELETE)
                .config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, "2")
                .build();
    }

    @Bean
    public DefaultErrorHandler kafkaErrorHandler() {
        return new DefaultErrorHandler((record, exception) -> LOGGER.error(
                "Kafka message failed permanently: topic={}, partition={}, offset={}",
                record.topic(), record.partition(), record.offset(), exception),
                new FixedBackOff(2000L, 3L));
    }
}