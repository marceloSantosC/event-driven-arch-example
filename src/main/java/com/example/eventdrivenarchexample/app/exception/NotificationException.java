package com.example.eventdrivenarchexample.app.exception;

import lombok.Getter;

public class NotificationException extends RuntimeException {

    @Getter
    private final boolean retryable;

    public NotificationException(String message, Throwable cause, boolean retryable) {
        super(message, cause);
        this.retryable = retryable;
    }

    public NotificationException(String message, boolean retryable) {
        super(message);
        this.retryable = retryable;
    }

}
