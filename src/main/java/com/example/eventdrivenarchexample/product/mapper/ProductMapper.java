package com.example.eventdrivenarchexample.product.mapper;


import com.example.eventdrivenarchexample.product.dto.events.NewProductEventPayload;
import com.example.eventdrivenarchexample.product.entity.ProductEntity;

public class ProductMapper {

    public static ProductEntity newProductDTOToProductEntity(NewProductEventPayload dto) {
        return ProductEntity.builder()
                .color(dto.color())
                .name(dto.name())
                .quantity(dto.quantity())
                .price(dto.price())
                .build();

    }

}
