package com.example.eventdrivenarchexample.product.dto.input;

import java.util.List;

public record TakeProductsInput(
        String eventId,
        String callbackQueue,
        List<Product> products


) {

    public record Product(
            Long id,
            Long quantity
    ) {
    }

}