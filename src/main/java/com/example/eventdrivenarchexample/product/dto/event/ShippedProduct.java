package com.example.eventdrivenarchexample.product.dto.event;

import com.example.eventdrivenarchexample.product.dto.command.ShipProducts;
import com.example.eventdrivenarchexample.product.entity.ProductEntity;
import com.example.eventdrivenarchexample.product.enumeration.ShippedProductStatus;
import lombok.*;

import java.math.BigDecimal;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShippedProduct {

    private Long id;
    private BigDecimal value;
    private String name;
    private String color;
    private ShippedProductStatus status;
    private Long quantityShipped;


    public static ShippedProduct valueOf(ProductEntity product, ShippedProductStatus status) {
        return ShippedProduct.builder()
                .color(product.getColor())
                .id(product.getId())
                .value(product.getPrice())
                .name(product.getName())
                .status(status)
                .build();
    }

    public static ShippedProduct valueOf(ShipProducts.Product product) {
        return ShippedProduct.builder()
                .id(product.id())
                .status(ShippedProductStatus.NOT_FOUND)
                .build();
    }

}