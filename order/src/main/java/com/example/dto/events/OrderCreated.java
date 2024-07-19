package com.example.dto.events;

import com.example.entity.OrderProductEntity;

import java.util.List;

public record OrderCreated(Long orderId, List<Product> products) {

    public static OrderCreated valueOf(Long id, List<OrderProductEntity> orderProducts) {
        List<Product> products = orderProducts.stream().map(Product::valueOf).toList();
        return new OrderCreated(id, products);
    }

    public record Product(Long id, Long quantity) {
        public static Product valueOf(OrderProductEntity product) {
            return new Product(product.getId().getProductId(), product.getQuantity());
        }
    }

}
