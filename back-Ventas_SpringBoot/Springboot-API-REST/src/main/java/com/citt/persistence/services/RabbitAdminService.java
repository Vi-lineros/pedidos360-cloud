package com.citt.persistence.services;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.stereotype.Service;

@Service
public class RabbitAdminService {

    private final AmqpAdmin amqpAdmin;

    public RabbitAdminService(AmqpAdmin amqpAdmin) {
        this.amqpAdmin = amqpAdmin;
    }

    public String createQueue(String queueName) {
        Queue newQueue = new Queue(queueName, true);
        return amqpAdmin.declareQueue(newQueue);
    }

    public boolean deleteQueue(String queueName) {
        if (amqpAdmin.getQueueProperties(queueName) == null) {
            return false;
        }

        amqpAdmin.deleteQueue(queueName);
        return true;
    }
}
