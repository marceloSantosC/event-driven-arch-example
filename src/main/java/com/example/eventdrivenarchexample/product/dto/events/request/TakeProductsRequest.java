package com.example.eventdrivenarchexample.product.dto.events.request;

import java.util.List;

public record TakeProductsRequest(
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