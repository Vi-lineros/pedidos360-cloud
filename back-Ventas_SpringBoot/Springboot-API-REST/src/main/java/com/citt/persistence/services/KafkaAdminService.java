package com.citt.persistence.services;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AlterConfigOp;
import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.clients.admin.ConsumerGroupDescription;
import org.apache.kafka.clients.admin.ConsumerGroupListing;
import org.apache.kafka.clients.admin.MemberDescription;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.ConfigResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
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
        try (AdminClient adminClient = crearAdminClient()) {
            return adminClient.listTopics().names().get();
        }
    }

    public void crearTopico(String nombre, int particiones, short replicas)
            throws ExecutionException, InterruptedException {
        String nombreValidado = validarNombreTopico(nombre);
        if (particiones < 1) {
            throw new IllegalArgumentException("La cantidad de particiones debe ser al menos 1");
        }
        if (replicas < 1) {
            throw new IllegalArgumentException("La cantidad de réplicas debe ser al menos 1");
        }

        try (AdminClient adminClient = crearAdminClient()) {
            NewTopic newTopic = new NewTopic(nombreValidado, particiones, replicas);
            adminClient.createTopics(Collections.singleton(newTopic)).all().get();
            log.info("Tópico {} creado con éxito", nombreValidado);
        }
    }

    public TopicDetails obtenerDetalleTopico(String nombre)
            throws ExecutionException, InterruptedException {
        String nombreValidado = validarNombreTopico(nombre);
        try (AdminClient adminClient = crearAdminClient()) {
            TopicDescription description = adminClient
                    .describeTopics(Collections.singleton(nombreValidado))
                    .allTopicNames()
                    .get()
                    .get(nombreValidado);
            Map<String, String> configurations = obtenerConfiguraciones(adminClient, nombreValidado);
            List<TopicPartitionDetails> partitions = description.partitions().stream()
                    .map(partition -> new TopicPartitionDetails(
                            partition.partition(),
                            partition.leader() == null ? null : partition.leader().id(),
                            partition.replicas().stream().map(Node::id).collect(Collectors.toList()),
                            partition.isr().stream().map(Node::id).collect(Collectors.toList())))
                    .collect(Collectors.toList());
            return new TopicDetails(description.name(), description.isInternal(), partitions, configurations);
        }
    }

    public Map<String, String> obtenerConfiguracionTopico(String nombre)
            throws ExecutionException, InterruptedException {
        String nombreValidado = validarNombreTopico(nombre);
        try (AdminClient adminClient = crearAdminClient()) {
            return obtenerConfiguraciones(adminClient, nombreValidado);
        }
    }

    public void eliminarTopico(String nombre) throws ExecutionException, InterruptedException {
        String nombreValidado = validarNombreTopico(nombre);
        try (AdminClient adminClient = crearAdminClient()) {
            adminClient.deleteTopics(Collections.singleton(nombreValidado)).all().get();
            log.info("Tópico {} eliminado con éxito", nombreValidado);
        }
    }

    public List<String> listarGruposConsumidores() throws ExecutionException, InterruptedException {
        try (AdminClient adminClient = crearAdminClient()) {
            return adminClient.listConsumerGroups().all().get()
                    .stream().map(ConsumerGroupListing::groupId).collect(Collectors.toList());
        }
    }

    public ConsumerGroupDetails obtenerGrupoConsumidor(String groupId)
            throws ExecutionException, InterruptedException {
        if (groupId == null || groupId.isBlank()) {
            throw new IllegalArgumentException("El ID del consumer group no puede estar vacío");
        }

        try (AdminClient adminClient = crearAdminClient()) {
            ConsumerGroupDescription description = adminClient
                    .describeConsumerGroups(Collections.singleton(groupId.trim()))
                    .describedGroups()
                    .get(groupId.trim())
                    .get();
            List<ConsumerGroupMember> members = description.members().stream()
                    .map(this::mapearMiembro)
                    .collect(Collectors.toList());
            Map<String, Map<Integer, Long>> offsets = mapearOffsets(adminClient
                    .listConsumerGroupOffsets(groupId.trim())
                    .partitionsToOffsetAndMetadata()
                    .get());

            return new ConsumerGroupDetails(
                    description.groupId(),
                    description.state().toString(),
                    members,
                    offsets);
        }
    }

    public void modificarRetencionTopico(String nombre, String retencionMs)
            throws ExecutionException, InterruptedException {
        modificarConfiguracionTopico(nombre, retencionMs, null);
    }

    public void modificarConfiguracionTopico(String nombre, String retencionMs, String cleanupPolicy)
            throws ExecutionException, InterruptedException {
        String nombreValidado = validarNombreTopico(nombre);
        List<AlterConfigOp> operations = new ArrayList<>();

        if (retencionMs != null) {
            operations.add(new AlterConfigOp(
                    new ConfigEntry("retention.ms", validarRetencion(retencionMs)),
                    AlterConfigOp.OpType.SET));
        }
        if (cleanupPolicy != null) {
            operations.add(new AlterConfigOp(
                    new ConfigEntry("cleanup.policy", validarCleanupPolicy(cleanupPolicy)),
                    AlterConfigOp.OpType.SET));
        }
        if (operations.isEmpty()) {
            throw new IllegalArgumentException("Debe indicar retention.ms o cleanup.policy para actualizar");
        }

        ConfigResource resource = new ConfigResource(ConfigResource.Type.TOPIC, nombreValidado);
        Map<ConfigResource, Collection<AlterConfigOp>> configs =
                Collections.singletonMap(resource, operations);
        try (AdminClient adminClient = crearAdminClient()) {
            adminClient.incrementalAlterConfigs(configs).all().get();
            log.info("Configuración del tópico {} actualizada", nombreValidado);
        }
    }

    private AdminClient crearAdminClient() {
        return AdminClient.create(kafkaAdmin.getConfigurationProperties());
    }

    private Map<String, String> obtenerConfiguraciones(AdminClient adminClient, String nombre)
            throws ExecutionException, InterruptedException {
        ConfigResource resource = new ConfigResource(ConfigResource.Type.TOPIC, nombre);
        Config config = adminClient.describeConfigs(Collections.singleton(resource))
                .all().get().get(resource);
        return config.entries().stream().collect(Collectors.toMap(
                ConfigEntry::name,
                ConfigEntry::value,
                (existing, replacement) -> replacement,
                LinkedHashMap::new));
    }

    private ConsumerGroupMember mapearMiembro(MemberDescription member) {
        List<AssignedPartition> assignment = member.assignment().topicPartitions().stream()
                .map(partition -> new AssignedPartition(partition.topic(), partition.partition()))
                .collect(Collectors.toList());
        return new ConsumerGroupMember(member.consumerId(), member.clientId(), member.host(), assignment);
    }

    private Map<String, Map<Integer, Long>> mapearOffsets(Map<TopicPartition, OffsetAndMetadata> groupOffsets) {
        Map<String, Map<Integer, Long>> offsets = new LinkedHashMap<>();
        groupOffsets.forEach((partition, metadata) -> offsets
                .computeIfAbsent(partition.topic(), topic -> new LinkedHashMap<>())
                .put(partition.partition(), metadata.offset()));
        return offsets;
    }

    private String validarNombreTopico(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del tópico no puede estar vacío");
        }
        return nombre.trim();
    }

    private String validarRetencion(String retencionMs) {
        try {
            long retention = Long.parseLong(retencionMs.trim());
            if (retention < 1) {
                throw new IllegalArgumentException("retention.ms debe ser mayor que 0");
            }
            return Long.toString(retention);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("retention.ms debe ser un número entero positivo", exception);
        }
    }

    private String validarCleanupPolicy(String cleanupPolicy) {
        String policy = cleanupPolicy.trim().toLowerCase(Locale.ROOT);
        if (!Set.of("delete", "compact", "compact,delete", "delete,compact").contains(policy)) {
            throw new IllegalArgumentException(
                    "cleanup.policy debe ser delete, compact o una combinación de ambas");
        }
        return policy;
    }

    public record TopicDetails(
            String name,
            boolean internal,
            List<TopicPartitionDetails> partitions,
            Map<String, String> configurations) {
    }

    public record TopicPartitionDetails(
            int partition,
            Integer leader,
            List<Integer> replicas,
            List<Integer> inSyncReplicas) {
    }

    public record ConsumerGroupDetails(
            String groupId,
            String state,
            List<ConsumerGroupMember> members,
            Map<String, Map<Integer, Long>> offsets) {
    }

    public record ConsumerGroupMember(
            String consumerId,
            String clientId,
            String host,
            List<AssignedPartition> assignment) {
    }

    public record AssignedPartition(String topic, int partition) {
    }
}
