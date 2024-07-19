package com.example.product.consumer.command;

import com.example.client.SQSClient;
import com.example.dto.command.Command;
import com.example.dto.event.Event;
import com.example.enumeration.EventResult;
import com.example.product.dto.command.NotifyProductEvent;
import com.example.product.dto.command.UserNotification;
import com.example.product.enumeration.ProductEventType;
import com.example.product.service.ProductNotificationService;
import lombok.RequiredArgsConstructor;

import java.util.Map;


@RequiredArgsConstructor
public abstract class CommandConsumer<O> {

    private final ProductNotificationService notificationService;

    private final SQSClient sqsClient;

    protected void sendNotification(UserNotification notificationBody, String traceId) {
        var notification = NotifyProductEvent.builder()
                .body(notificationBody)
                .traceId(traceId)
                .build();
        notificationService.send(notification);
    }

    protected void sendEvent(Command<?> command, O output, EventResult result) {
        Event<O, ProductEventType> eventPayload = Event.<O, ProductEventType>builder()
                .customId(command.getCustomId())
                .eventType(getEventType())
                .eventResult(result)
                .body(output)
                .build();

        sqsClient.sendToSQS(getEventQueue(), eventPayload, Map.of(SQSClient.HEADER_TRACE_ID_NAME, command.getTraceId()));
    }

    protected abstract ProductEventType getEventType();

    protected abstract String getEventQueue();

}
