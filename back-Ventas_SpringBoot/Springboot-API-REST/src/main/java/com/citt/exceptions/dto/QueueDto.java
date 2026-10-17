package com.citt.exceptions.dto;

import jakarta.validation.constraints.NotBlank;

public class QueueDto {

    @NotBlank(message = "El nombre de la cola no puede estar vacío")
    private String queueName;

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }
}
