package com.example.eventdrivenarchexample.product.consumer.command;

import com.example.eventdrivenarchexample.app.client.SQSClient;
import com.example.eventdrivenarchexample.product.dto.command.CommandPayload;
import com.example.eventdrivenarchexample.product.dto.command.NotifyProduct;
import com.example.eventdrivenarchexample.product.dto.command.NotifyProductNotification;
import com.example.eventdrivenarchexample.product.dto.event.EventPayload;
import com.example.eventdrivenarchexample.product.enumeration.ProductEventResult;
import com.example.eventdrivenarchexample.product.enumeration.ProductEventType;
import com.example.eventdrivenarchexample.product.service.ProductNotificationService;
import lombok.RequiredArgsConstructor;

import java.util.Map;


@RequiredArgsConstructor
public abstract class CommandConsumer<O> {

    private final ProductNotificationService notificationService;

    private final SQSClient sqsClient;

    protected void sendNotification(NotifyProductNotification notificationBody, String traceId) {
        var notification = NotifyProduct.builder()
                .body(notificationBody)
                .traceId(traceId)
                .build();
        notificationService.send(notification);
    }

    protected void sendEvent(CommandPayload<?> command, O output, ProductEventResult result) {
        EventPayload<O> eventPayload = EventPayload.<O>builder()
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
