package com.example.eventdrivenarchexample.product.dto.output;

import com.example.eventdrivenarchexample.product.dto.input.TakeProductsInput;
import com.example.eventdrivenarchexample.product.entity.ProductEntity;
import com.example.eventdrivenarchexample.product.enumeration.TakeProductStatus;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record TakenProductOutput(
        Long id,
        BigDecimal value,
        String name,
        String color,
        TakeProductStatus status
) {

    public static TakenProductOutput valueOf(ProductEntity product, TakeProductStatus status) {
        return TakenProductOutput.builder()
                .color(product.getColor())
                .id(product.getId())
                .value(product.getPrice())
                .name(product.getName())
                .status(status)
                .build();
    }

    public static TakenProductOutput valueOf(TakeProductsInput.Product product) {
        return TakenProductOutput.builder()
                .id(product.id())
                .status(TakeProductStatus.NOT_FOUND)
                .build();
    }
}