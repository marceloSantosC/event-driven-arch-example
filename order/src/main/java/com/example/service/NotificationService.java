package com.example.service;

import com.example.client.SQSClient;
import com.example.dto.command.Command;
import com.example.dto.command.UserNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    @Value("${sqs-queues.notification.commands.notify-user}")
    private String notificationQueue;

    private final SQSClient sqsClient;

    public void send(UserNotification notification, String traceId) {
        log.info("Received notification with trace id {}.", traceId);
        Map<String, Object> messageHeaders = Map.of(SQSClient.HEADER_TRACE_ID_NAME, traceId);

        var command = Command.<UserNotification>builder()
                .body(notification)
                .build();

        sqsClient.sendToSQS(notificationQueue, command, messageHeaders);
        log.info("Notification with trace id {} sent.", traceId);
    }


}
