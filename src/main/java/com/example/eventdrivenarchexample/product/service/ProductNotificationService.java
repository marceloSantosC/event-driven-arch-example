package com.example.eventdrivenarchexample.product.service;

import com.example.eventdrivenarchexample.app.client.SQSClient;
import com.example.eventdrivenarchexample.product.config.ProductCommandQueues;
import com.example.eventdrivenarchexample.product.dto.command.CommandPayload;
import com.example.eventdrivenarchexample.product.dto.command.NotifyProduct;
import com.example.eventdrivenarchexample.product.dto.command.NotifyProductNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductNotificationService {

    private final ProductCommandQueues productCommandQueues;

    private final SQSClient sqsClient;

    public void send(NotifyProduct notification) {
        log.info("Received notification with trace id {}.", notification.traceId());
        Map<String, Object> messageHeaders = Map.of(SQSClient.HEADER_TRACE_ID_NAME, notification.traceId());

        CommandPayload<NotifyProductNotification> command = new CommandPayload<>();
        command.setBody(notification.body());

        sqsClient.sendToSQS(productCommandQueues.getNotify(), command, messageHeaders);
        log.info("Notification with trace id {} sent.", notification.traceId());
    }


}
