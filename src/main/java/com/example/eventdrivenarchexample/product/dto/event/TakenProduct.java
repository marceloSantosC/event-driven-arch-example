package com.example.eventdrivenarchexample.product.dto.event;

import com.example.eventdrivenarchexample.product.dto.command.TakeProducts;
import com.example.eventdrivenarchexample.product.entity.ProductEntity;
import com.example.eventdrivenarchexample.product.enumeration.TakeProductStatus;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record TakenProduct(
        Long id,
        BigDecimal value,
        String name,
        String color,
        TakeProductStatus status
) {

    public static TakenProduct valueOf(ProductEntity product, TakeProductStatus status) {
        return TakenProduct.builder()
                .color(product.getColor())
                .id(product.getId())
                .value(product.getPrice())
                .name(product.getName())
                .status(status)
                .build();
    }

    public static TakenProduct valueOf(TakeProducts.Product product) {
        return TakenProduct.builder()
                .id(product.id())
                .status(TakeProductStatus.NOT_FOUND)
                .build();
    }
}