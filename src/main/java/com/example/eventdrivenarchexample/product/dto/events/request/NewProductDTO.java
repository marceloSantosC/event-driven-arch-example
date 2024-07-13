package com.example.eventdrivenarchexample.product.dto.events.request;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record NewProductDTO(

        String name,

        String color,

        Long quantity,

        BigDecimal price,

        String eventId,

        String callbackQueue

) {
}
