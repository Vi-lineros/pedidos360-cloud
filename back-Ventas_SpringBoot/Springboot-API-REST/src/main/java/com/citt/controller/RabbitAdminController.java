package com.citt.controller;

import com.citt.exceptions.dto.QueueDto;
import com.citt.persistence.services.RabbitAdminService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/queues")
public class RabbitAdminController {

    private final RabbitAdminService rabbitAdminService;

    public RabbitAdminController(RabbitAdminService rabbitAdminService) {
        this.rabbitAdminService = rabbitAdminService;
    }

    @PostMapping
    public ResponseEntity<String> createQueue(@Valid @RequestBody QueueDto queueDto) {
        rabbitAdminService.createQueue(queueDto.getQueueName());
        return ResponseEntity.ok("Cola '" + queueDto.getQueueName() + "' creada exitosamente.");
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<String> deleteQueue(@PathVariable String name) {
        return rabbitAdminService.deleteQueue(name)
                ? ResponseEntity.ok("Cola eliminada.")
                : ResponseEntity.badRequest().body("Error al eliminar.");
    }
}
