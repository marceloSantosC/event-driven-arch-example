package com.example.eventdrivenarchexample.product.dto.events.request;

import com.example.eventdrivenarchexample.product.enumeration.ProductEventResult;
import com.example.eventdrivenarchexample.product.enumeration.ProductEventType;
import lombok.Builder;


@Builder
public record ProductNotificationDTO(ProductEventType eventType,
                                     ProductEventResult result,
                                     NotificationBodyDTO body,
                                     String[] titleArgs,
                                     String[] messageArgs,
                                     String traceId) {
}
