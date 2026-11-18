package com.citt.persistence.services;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.config.ConfigResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Slf4j
@Service
public class KafkaAdminService {

    @Autowired
    private KafkaAdmin kafkaAdmin;

    public Set<String> listarTopicos() throws ExecutionException, InterruptedException {
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            return adminClient.listTopics().names().get();
        }
    }

    public void crearTopico(String nombre, int particiones, short replicas) throws ExecutionException, InterruptedException {
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            NewTopic newTopic = new NewTopic(nombre, particiones, replicas);
            adminClient.createTopics(Collections.singleton(newTopic)).all().get();
            log.info("Tópico {} creado con éxito", nombre);
        }
    }

    public void eliminarTopico(String nombre) throws ExecutionException, InterruptedException {
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            adminClient.deleteTopics(Collections.singleton(nombre)).all().get();
            log.info("Tópico {} eliminado con éxito", nombre);
        }
    }

    public List<String> listarGruposConsumidores() throws ExecutionException, InterruptedException {
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            return adminClient.listConsumerGroups().all().get()
                    .stream().map(ConsumerGroupListing::groupId).collect(Collectors.toList());
        }
    }
    
    public void modificarRetencionTopico(String nombre, String retencionMs) throws ExecutionException, InterruptedException {
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            ConfigResource resource = new ConfigResource(ConfigResource.Type.TOPIC, nombre);
            ConfigEntry entry = new ConfigEntry("retention.ms", retencionMs);
            AlterConfigOp op = new AlterConfigOp(entry, AlterConfigOp.OpType.SET);
            
            Map<ConfigResource, java.util.Collection<AlterConfigOp>> configs = Collections.singletonMap(resource, Collections.singleton(op));
            adminClient.incrementalAlterConfigs(configs).all().get();
            log.info("Retención del tópico {} modificada a {} ms", nombre, retencionMs);
        }
    }
}