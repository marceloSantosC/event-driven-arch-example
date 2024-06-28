package com.example.eventdrivenarchexample.product.dto.events;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UpdateProductEventPayload(

        Long id,

        String name,

        String color,

        Long quantity,

        BigDecimal price,

        String callbackQueue,

        String eventId

) {
}
