package com.example.eventdrivenarchexample.product.service;

import com.example.eventdrivenarchexample.app.client.SQSClient;
import com.example.eventdrivenarchexample.product.config.ProductQueueProperties;
import com.example.eventdrivenarchexample.product.dto.events.request.ProductNotificationDTO;
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

    public void send(ProductNotificationDTO notification) {
        log.info("Received notification with trace id {} for event {} with result {}.",
                notification.traceId(), notification.eventType(), notification.result());
        Map<String, Object> messageHeaders = Map.of(SQSClient.HEADER_TRACE_ID_NAME, notification.traceId());
        sqsClient.sendToSQS(productQueues.getNotificationEventsQueue(), notification.body(), messageHeaders);
        log.info("Notification ith trace id {} for event {} with result {} sent.",
                notification.traceId(), notification.eventType(), notification.result());
    }


}
