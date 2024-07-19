package com.example.product.dto.event;

import com.example.enumeration.EventResult;
import com.example.product.enumeration.ProductEventType;
import lombok.Builder;

@Builder
public record UpdatedProductEvent(

        String eventId,

        String customId,

        Long productId,

        ProductEventType eventType,

        EventResult eventResult

) {

}