package com.example.eventdrivenarchexample.product.exception;

import lombok.Getter;

@Getter
public class ProductException extends RuntimeException {

    private final boolean retryable;

    private final String reason;


    public ProductException(String message, boolean retryable, String reason) {
        super(message);
        this.retryable = retryable;
        this.reason = reason;
    }

}
