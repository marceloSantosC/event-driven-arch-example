package com.example.dto.command;

import com.example.dto.event.OrderCreated;

import java.util.List;

public record ShipProducts(
        List<Product> products
) {

    public static ShipProducts valueOf(OrderCreated orderCreated) {
        var products = orderCreated.products().stream()
                .map(product -> new Product(product.id(), product.quantity()))
                .toList();
        return new ShipProducts(products);
    }

    public record Product(
            Long id,
            Long quantity
    ) {
    }

}