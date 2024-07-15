package com.example.eventdrivenarchexample.product.listener;

import com.example.eventdrivenarchexample.app.client.SQSClient;
import com.example.eventdrivenarchexample.product.dto.command.CommandPayload;
import com.example.eventdrivenarchexample.product.dto.command.NotifyProduct;
import com.example.eventdrivenarchexample.product.dto.command.NotifyProductNotification;
import com.example.eventdrivenarchexample.product.dto.event.EventPayload;
import com.example.eventdrivenarchexample.product.enumeration.ProductEventResult;
import com.example.eventdrivenarchexample.product.enumeration.ProductEventType;
import com.example.eventdrivenarchexample.product.service.ProductNotificationService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;


@RequiredArgsConstructor
public abstract class EventListener<O> {

    private final ProductNotificationService notificationService;

    private final SQSClient sqsClient;

    protected void sendNotification(NotifyProductNotification notificationBody, String traceId) {
        var notification = NotifyProduct.builder()
                .body(notificationBody)
                .traceId(traceId)
                .build();
        notificationService.send(notification);
    }

    protected void tryToSendEventResultToCallbackQueue(CommandPayload<?> event, O output, ProductEventResult result) {
        if (StringUtils.isBlank(event.getCallbackQueue())) return;

        EventPayload<O> callbackPayload = EventPayload.<O>builder()
                .customId(event.getCustomId())
                .eventType(getEventType())
                .eventResult(result)
                .body(output)
                .build();

        sqsClient.sendToSQS(event.getCallbackQueue(), callbackPayload, Map.of(SQSClient.HEADER_TRACE_ID_NAME, event.getTraceId()));
    }

    protected abstract ProductEventType getEventType();

}
