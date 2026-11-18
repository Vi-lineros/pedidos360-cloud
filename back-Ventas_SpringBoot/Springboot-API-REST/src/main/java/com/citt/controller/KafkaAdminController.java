package com.citt.controller;

import com.citt.persistence.services.KafkaAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}