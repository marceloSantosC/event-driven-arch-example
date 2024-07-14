package com.example.eventdrivenarchexample.product.dto.output;

import com.example.eventdrivenarchexample.product.enumeration.ProductEventResult;
import com.example.eventdrivenarchexample.product.enumeration.ProductEventType;
import lombok.Builder;

@Builder
public record UpdatedProductEvent(

        String eventId,

        String customId,

        Long productId,

        ProductEventType eventType,

        ProductEventResult eventResult

) {

}