package com.example.eventdrivenarchexample.product.listener;

import com.example.eventdrivenarchexample.product.dto.input.NotificationBodyInput;
import com.example.eventdrivenarchexample.product.dto.input.ProductNotificationInput;
import com.example.eventdrivenarchexample.product.enumeration.ProductEventType;
import com.example.eventdrivenarchexample.product.service.ProductNotificationService;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public abstract class EventListenerWithNotification {

    private final ProductNotificationService notificationService;

    protected void sendNotification(NotificationBodyInput notificationBody, String traceId) {
        var notification = ProductNotificationInput.builder()
                .body(notificationBody)
                .traceId(traceId)
                .build();
        notificationService.send(notification);
    }

    protected abstract ProductEventType getEventType();

}
