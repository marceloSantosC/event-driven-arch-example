package com.example.eventdrivenarchexample.product.dto.command;

import java.util.List;

public record ShipProducts(
        List<Product> products


) {

    public record Product(
            Long id,
            Long quantity
    ) {
    }

}