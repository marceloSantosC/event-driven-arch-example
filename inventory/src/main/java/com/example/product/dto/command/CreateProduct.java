package com.example.product.dto.command;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record CreateProduct(

        String name,

        String color,

        Long quantity,

        BigDecimal price,

        String eventId,

        String callbackQueue

) {
}
