package com.example.eventdrivenarchexample.product.dto.input;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UpdateProductInput(

        Long id,

        String name,

        String color,

        Long quantity,

        BigDecimal price,

        String callbackQueue,

        String eventId

) {
}
