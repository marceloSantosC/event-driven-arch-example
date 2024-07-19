package com.example.dto.events;


import com.example.enumeration.CustomerDocumentType;
import lombok.Builder;

import java.util.List;

@Builder
public record OrderReceived(
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

