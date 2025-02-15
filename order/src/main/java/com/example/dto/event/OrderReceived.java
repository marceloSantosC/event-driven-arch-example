package com.example.dto.event;


import com.example.enumeration.CustomerDocumentType;
import lombok.Builder;

import java.util.List;

@Builder
public record OrderReceived(
        List<Product> products,
        String customerDocument,
        CustomerDocumentType documentType
) {
    public record Product(
            Long id,
            Long quantity
    ) {
    }
}

