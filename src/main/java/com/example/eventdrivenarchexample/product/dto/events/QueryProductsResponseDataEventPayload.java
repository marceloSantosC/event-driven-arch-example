package com.example.eventdrivenarchexample.product.dto.events;

import com.example.eventdrivenarchexample.product.entity.ProductEntity;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;


public record QueryProductsResponseDataEventPayload(
        String eventId,
        List<Product> products
) {
    public static QueryProductsResponseDataEventPayload valueOf(String eventId, List<ProductEntity> products) {
        var productsResponse = products.stream().map(Product::valueOf).toList();
        return new QueryProductsResponseDataEventPayload(eventId, productsResponse);
    }

}

@Builder
record Product(
        Long id,
        String name,
        String color,
        Long quantityAvailable,
        BigDecimal price
) {
    public static Product valueOf(ProductEntity productEntity) {
        return Product.builder()
                .id(productEntity.getId())
                .name(productEntity.getName())
                .color(productEntity.getColor())
                .quantityAvailable(productEntity.getQuantity())
                .price(productEntity.getPrice())
                .build();
    }
}
