package com.example.eventdrivenarchexample.product.service;

import com.example.eventdrivenarchexample.app.client.SQSClient;
import com.example.eventdrivenarchexample.product.config.ProductQueueProperties;
import com.example.eventdrivenarchexample.product.dto.input.EventPayload;
import com.example.eventdrivenarchexample.product.dto.input.NotificationBodyInput;
import com.example.eventdrivenarchexample.product.dto.input.ProductNotificationInput;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductNotificationService {

    private final ProductQueueProperties productQueues;

    private final SQSClient sqsClient;

    public void send(ProductNotificationInput notification) {
        log.info("Received notification with trace id {}.", notification.traceId());
        Map<String, Object> messageHeaders = Map.of(SQSClient.HEADER_TRACE_ID_NAME, notification.traceId());

        EventPayload<NotificationBodyInput> event = new EventPayload<>();
        event.setBody(notification.body());

        sqsClient.sendToSQS(productQueues.getNotificationEventsQueue(), event, messageHeaders);
        log.info("Notification with trace id {} sent.", notification.traceId());
    }


}
