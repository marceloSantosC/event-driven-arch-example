package com.example.eventdrivenarchexample.product.dto.events;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record NewProductEventPayload(

        String name,

        String color,

        Long quantity,

        BigDecimal price,

        String eventId

) {
}
