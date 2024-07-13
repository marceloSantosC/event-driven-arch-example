package com.example.eventdrivenarchexample.product.listener;

import com.example.eventdrivenarchexample.product.dto.events.request.NotificationBodyDTO;
import com.example.eventdrivenarchexample.product.dto.events.request.ProductNotificationDTO;
import com.example.eventdrivenarchexample.product.enumeration.ProductEventResult;
import com.example.eventdrivenarchexample.product.enumeration.ProductEventType;
import com.example.eventdrivenarchexample.product.service.ProductNotificationService;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public abstract class EventListenerWithNotification {

    private final ProductNotificationService notificationService;

    protected void sendNotification(NotificationBodyDTO notificationBody, String traceId, ProductEventResult result) {
        var notification = ProductNotificationDTO.builder()
                .eventType(getEventType())
                .result(result)
                .body(notificationBody)
                .traceId(traceId)
                .build();
        notificationService.send(notification);
    }

    protected abstract ProductEventType getEventType();

}
