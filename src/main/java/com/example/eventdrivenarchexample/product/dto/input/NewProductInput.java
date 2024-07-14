package com.example.eventdrivenarchexample.product.dto.input;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record NewProductInput(

        String name,

        String color,

        Long quantity,

        BigDecimal price,

        String eventId,

        String callbackQueue

) {
}
