package com.example.eventdrivenarchexample.app.exception;

import lombok.Getter;

@Getter
public class MessageBrokerException extends RuntimeException {

    private final String queueName;

    public MessageBrokerException(String message, String queueName) {
        super(message);
        this.queueName = queueName;
    }

    public MessageBrokerException(String message, String queueName, Exception e) {
        super(message, e);
        this.queueName = queueName;
    }

}
