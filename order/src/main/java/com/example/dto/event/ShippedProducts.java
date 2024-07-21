package com.example.dto.event;

import com.example.enumeration.OrderProductStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

public record ShippedProducts(Set<ShippedProduct> products,
                              LocalDateTime date) {

    public record ShippedProduct(Long id,
                                 BigDecimal value,
                                 String name,
                                 String color,
                                 OrderProductStatus status,
                                 Long quantityShipped) {

    }

}
