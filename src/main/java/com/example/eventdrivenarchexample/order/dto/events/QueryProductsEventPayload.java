package com.example.eventdrivenarchexample.order.dto.events;

import lombok.Builder;

import java.util.List;


@Builder
public record QueryProductsEventPayload(
        String eventId,
        String callbackQueue,
        List<Long> productIds
) {
}