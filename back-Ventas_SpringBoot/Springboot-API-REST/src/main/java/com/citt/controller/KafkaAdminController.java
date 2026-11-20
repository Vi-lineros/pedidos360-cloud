package com.citt.controller;

import com.citt.persistence.services.KafkaAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/kafka/admin")
@Tag(name = "Kafka Admin", description = "Endpoints para administrar el clúster de Kafka dinámicamente")
public class KafkaAdminController {

    @Autowired
    private KafkaAdminService kafkaAdminService;

    @GetMapping("/topicos")
    @Operation(summary = "Listar todos los tópicos existentes en el broker")
    public ResponseEntity<Set<String>> listarTopicos() throws Exception {
        return ResponseEntity.ok(kafkaAdminService.listarTopicos());
    }

    @GetMapping("/topicos/{nombre}")
    @Operation(summary = "Inspeccionar un tópico, sus particiones y configuración")
    public ResponseEntity<KafkaAdminService.TopicDetails> obtenerDetalleTopico(
            @PathVariable String nombre) throws Exception {
        return ResponseEntity.ok(kafkaAdminService.obtenerDetalleTopico(nombre));
    }

    @GetMapping("/topicos/{nombre}/configuracion")
    @Operation(summary = "Consultar la configuración de un tópico")
    public ResponseEntity<Map<String, String>> obtenerConfiguracionTopico(
            @PathVariable String nombre) throws Exception {
        return ResponseEntity.ok(kafkaAdminService.obtenerConfiguracionTopico(nombre));
    }

    @PostMapping("/topicos")
    @Operation(summary = "Crear un nuevo tópico definiendo particiones y réplicas")
    public ResponseEntity<String> crearTopico(@RequestParam String nombre, @RequestParam int particiones, @RequestParam short replicas) throws Exception {
        kafkaAdminService.crearTopico(nombre, particiones, replicas);
        return ResponseEntity.ok("Tópico creado exitosamente");
    }

    @DeleteMapping("/topicos/{nombre}")
    @Operation(summary = "Eliminar un tópico por su nombre")
    public ResponseEntity<String> eliminarTopico(@PathVariable String nombre) throws Exception {
        kafkaAdminService.eliminarTopico(nombre);
        return ResponseEntity.ok("Tópico eliminado exitosamente");
    }

    @GetMapping("/grupos")
    @Operation(summary = "Listar grupos de consumidores activos (Consumer Groups)")
    public ResponseEntity<List<String>> listarGrupos() throws Exception {
        return ResponseEntity.ok(kafkaAdminService.listarGruposConsumidores());
    }
    
    @PutMapping("/topicos/{nombre}/retencion")
    @Operation(summary = "Modificar el tiempo de retención de un tópico (en milisegundos)")
    public ResponseEntity<String> modificarRetencion(@PathVariable String nombre, @RequestParam String ms) throws Exception {
        kafkaAdminService.modificarRetencionTopico(nombre, ms);
        return ResponseEntity.ok("Retención modificada exitosamente");
    }

    @PutMapping("/topicos/{nombre}/configuracion")
    @Operation(summary = "Actualizar retention.ms y/o cleanup.policy de un tópico")
    public ResponseEntity<String> modificarConfiguracionTopico(
            @PathVariable String nombre,
            @RequestParam(required = false) String retencionMs,
            @RequestParam(required = false) String cleanupPolicy) throws Exception {
        kafkaAdminService.modificarConfiguracionTopico(nombre, retencionMs, cleanupPolicy);
        return ResponseEntity.ok("Configuración del tópico actualizada exitosamente");
    }

    @PutMapping("/topicos/{nombre}/cleanup-policy")
    @Operation(summary = "Actualizar cleanup.policy de un tópico")
    public ResponseEntity<String> modificarCleanupPolicy(
            @PathVariable String nombre,
            @RequestParam String politica) throws Exception {
        kafkaAdminService.modificarConfiguracionTopico(nombre, null, politica);
        return ResponseEntity.ok("cleanup.policy modificada exitosamente");
    }

    @GetMapping("/consumer-groups")
    @Operation(summary = "Listar consumer groups")
    public ResponseEntity<List<String>> listarConsumerGroups() throws Exception {
        return ResponseEntity.ok(kafkaAdminService.listarGruposConsumidores());
    }

    @GetMapping("/consumer-groups/{groupId}")
    @Operation(summary = "Inspeccionar estado, miembros y offsets de un consumer group")
    public ResponseEntity<KafkaAdminService.ConsumerGroupDetails> obtenerConsumerGroup(
            @PathVariable String groupId) throws Exception {
        return ResponseEntity.ok(kafkaAdminService.obtenerGrupoConsumidor(groupId));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> manejarEntradaInvalida(IllegalArgumentException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
    }
}