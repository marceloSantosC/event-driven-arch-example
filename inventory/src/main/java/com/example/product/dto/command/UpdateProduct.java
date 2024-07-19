package com.example.product.dto.command;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UpdateProduct(

        Long id,

        String name,

        String color,

        Long quantity,

        BigDecimal price,

        String callbackQueue,

        String eventId

) {
}
