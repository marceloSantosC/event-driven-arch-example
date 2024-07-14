package com.example.eventdrivenarchexample.product.listener;

import com.example.eventdrivenarchexample.app.client.SQSClient;
import com.example.eventdrivenarchexample.product.dto.input.EventPayload;
import com.example.eventdrivenarchexample.product.dto.input.NotificationBodyInput;
import com.example.eventdrivenarchexample.product.dto.input.ProductNotificationInput;
import com.example.eventdrivenarchexample.product.dto.output.EventCallbackPayload;
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

    protected void sendNotification(NotificationBodyInput notificationBody, String traceId) {
        var notification = ProductNotificationInput.builder()
                .body(notificationBody)
                .traceId(traceId)
                .build();
        notificationService.send(notification);
    }

    protected void tryToSendEventResultToCallbackQueue(EventPayload<?> event, O output, ProductEventResult result) {
        if (StringUtils.isBlank(event.getCallbackQueue())) return;

        EventCallbackPayload<O> callbackPayload = EventCallbackPayload.<O>builder()
                .customId(event.getCustomId())
                .eventType(getEventType())
                .eventResult(result)
                .body(output)
                .build();

        sqsClient.sendToSQS(event.getCallbackQueue(), callbackPayload, Map.of(SQSClient.HEADER_TRACE_ID_NAME, event.getTraceId()));
    }

    protected abstract ProductEventType getEventType();

}
