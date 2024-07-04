package com.example.eventdrivenarchexample.order.dto.events;

import com.example.eventdrivenarchexample.order.enumeration.CustomerDocumentType;
import lombok.Builder;

import java.util.List;

@Builder
public record NewOrderEventPayload(
        String eventId,
        List<Product> products,
        String customerDocument,
        CustomerDocumentType documentType
) {
    public record Product(
            Long id,
            Integer quantity
    ) {
    }
}

