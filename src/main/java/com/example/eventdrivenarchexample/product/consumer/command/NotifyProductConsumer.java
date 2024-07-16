package com.example.eventdrivenarchexample.product.consumer.command;

import com.example.eventdrivenarchexample.app.client.SQSClient;
import com.example.eventdrivenarchexample.app.exception.NotificationException;
import com.example.eventdrivenarchexample.app.service.NotificationService;
import com.example.eventdrivenarchexample.product.dto.command.CommandPayload;
import com.example.eventdrivenarchexample.product.dto.command.NotifyProductNotification;
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

import static com.example.eventdrivenarchexample.product.enumeration.ProductEventType.NOTIFICATION;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotifyProductConsumer {

    private final Set<NotificationService> notificationServices;

    private final ObjectMapper objectMapper;

    @SqsListener("${sqs-queues.product.commands.notify}")
    public void notifyProductEvent(@Payload String payload, @Headers Map<String, Object> headers) {
        String traceId = (String) headers.get(SQSClient.HEADER_TRACE_ID_NAME);
        log.info("Sending notifications for event with trace id {}.", traceId);
        readPayloadFromJson(payload, traceId)
                .ifPresent((event -> notificationServices.forEach(notificationService -> {
                    try {
                        notificationService.send(event.getBody());
                    } catch (NotificationException e) {
                        log.error("Service {} failed to send notification : {}. Retryable? {}. Trace id: {}.",
                                notificationService.getClass(), e.getMessage(), e.isRetryable(), traceId);
                    }
                })));
        log.info("Sent notifications for event with trace id {}.", traceId);

    }

    private Optional<CommandPayload<NotifyProductNotification>> readPayloadFromJson(String json, String traceId) {
        try {
            var event = objectMapper.readValue(json, new TypeReference<CommandPayload<NotifyProductNotification>>() {
            });
            return Optional.of(event);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize message for product event {} with trace id {}.", NOTIFICATION, traceId);
            return Optional.empty();
        }
    }

}
