package com.example.eventdrivenarchexample.product.dto.output;

import com.example.eventdrivenarchexample.product.enumeration.ProductEventResult;
import com.example.eventdrivenarchexample.product.enumeration.ProductEventType;
import lombok.Builder;

import java.util.List;

@Builder
public record TakenProductsEvent(
        ProductEventResult result,
        ProductEventType type,
        String customId,
        List<TakenProductOutput> products
) {
}
