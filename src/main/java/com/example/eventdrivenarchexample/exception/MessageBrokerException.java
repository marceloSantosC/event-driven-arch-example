package com.example.eventdrivenarchexample.exception;

import lombok.Getter;

@Getter
public class MessageBrokerException extends RuntimeException {

    private final String queueName;

    public MessageBrokerException(String message, String queueName, Throwable cause) {
        super(message, cause);
        this.queueName = queueName;
    }

    public MessageBrokerException(String message, String queueName) {
        super(message);
        this.queueName = queueName;
    }

}
