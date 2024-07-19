package com.example.product.service;

import com.example.client.SQSClient;
import com.example.dto.command.Command;
import com.example.product.dto.command.NotifyProductEvent;
import com.example.product.dto.command.UserNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductNotificationService {

    @Value("${sqs-queues.notification.commands.notify-user}")
    private String notificationQueue;

    private final SQSClient sqsClient;

    public void send(NotifyProductEvent notification) {
        log.info("Received notification with trace id {}.", notification.traceId());
        Map<String, Object> messageHeaders = Map.of(SQSClient.HEADER_TRACE_ID_NAME, notification.traceId());

        Command<UserNotification> command = new Command<>();
        command.setBody(notification.body());

        sqsClient.sendToSQS(notificationQueue, command, messageHeaders);
        log.info("Notification with trace id {} sent.", notification.traceId());
    }


}
