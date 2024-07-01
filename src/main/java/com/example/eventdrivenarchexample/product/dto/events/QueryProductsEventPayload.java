package com.example.eventdrivenarchexample.product.dto.events;

import java.util.List;

public record QueryProductsEventPayload(
        String eventId,
        String callbackQueue,
        List<Long> productIds
) {
}