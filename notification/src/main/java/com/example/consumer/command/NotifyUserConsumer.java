package com.example.consumer.command;

import com.example.client.SQSClient;
import com.example.dto.command.Command;
import com.example.dto.command.UserNotification;
import com.example.exception.NotificationException;
import com.example.service.NotificationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotifyUserConsumer {

    private final Set<NotificationService> notificationServices;

    private final ObjectMapper objectMapper;

    @SqsListener("${sqs-queues.commands.notify-user}")
    public void notifyUser(@Payload String payload, @Headers Map<String, Object> headers) {
        String traceId = (String) headers.get(SQSClient.HEADER_TRACE_ID_NAME);
        log.info("Sending notifications for event with trace id {}.", traceId);
        readPayloadFromJson(payload, traceId)
                .ifPresent((command -> notificationServices.stream()
                        .filter(notificationService -> notificationService.canSendNotification(command.getBody()))
                        .forEach(notificationService -> {
                            try {
                                notificationService.send(command.getBody());
                                log.info("Service {} sent notification. Trace id: {}.", notificationService.getClass(), traceId);
                            } catch (NotificationException e) {
                                log.error("Service {} failed to send notification: {}. Trace id: {}.",
                                        notificationService.getClass(), e.getMessage(), traceId);
                            }
                        })));

    }

    private Optional<Command<UserNotification>> readPayloadFromJson(String json, String traceId) {
        try {
            var command = objectMapper.readValue(json, new TypeReference<Command<UserNotification>>() {
            });
            return Optional.of(command);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize message with trace id {}.", traceId);
            return Optional.empty();
        }
    }

}
