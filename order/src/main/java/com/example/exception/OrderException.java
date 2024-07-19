package com.example.exception;

import lombok.Getter;

@Getter
public class OrderException extends RuntimeException {

    private final boolean retryable;


    public OrderException(String message, boolean retryable) {
        super(message);
        this.retryable = retryable;
    }
}
