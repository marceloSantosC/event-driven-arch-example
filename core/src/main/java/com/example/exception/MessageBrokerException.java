package com.example.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Getter;

@Getter
public class MessageBrokerException extends RuntimeException {

    private final String queueName;

    public MessageBrokerException(String message, String queueName) {
        super(message);
        this.queueName = queueName;
    }

    public MessageBrokerException(JsonProcessingException e, String queueName) {
        super(e.getMessage(), e);
        this.queueName = queueName;
    }
}
