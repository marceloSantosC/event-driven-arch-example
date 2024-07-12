package com.example.eventdrivenarchexample.product.dto.events.request;

import com.example.eventdrivenarchexample.product.enumeration.ProductEventResult;
import com.example.eventdrivenarchexample.product.enumeration.ProductEventType;
import lombok.Builder;

@Builder
public record ProductEventCallbackRequest(

        String eventId,

        Long productId,

        ProductEventType eventType,

        ProductEventResult eventResult

) {

}