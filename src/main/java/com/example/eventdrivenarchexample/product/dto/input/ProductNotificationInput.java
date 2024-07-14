package com.example.eventdrivenarchexample.product.dto.input;

import lombok.Builder;


@Builder
public record ProductNotificationInput(NotificationBodyInput body,
                                       String traceId) {
}
