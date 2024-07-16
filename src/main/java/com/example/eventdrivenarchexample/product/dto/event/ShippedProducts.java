package com.example.eventdrivenarchexample.product.dto.event;

import java.time.LocalDateTime;
import java.util.Set;

public record ShippedProducts(Set<ShippedProduct> products,
                              LocalDateTime date) {
}
