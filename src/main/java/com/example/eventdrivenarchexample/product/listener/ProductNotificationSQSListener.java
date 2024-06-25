package com.example.eventdrivenarchexample.product.listener;

import com.example.eventdrivenarchexample.app.dto.NotificationDTO;
import com.example.eventdrivenarchexample.app.exception.NotificationException;
import com.example.eventdrivenarchexample.app.service.NotificationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductNotificationSQSListener {

    private final Set<NotificationService> notificationServices;

    private final ObjectMapper objectMapper;

    @SqsListener("${event-queues.product.notification-events}")
    public void notifyEvent(@Payload String payload) throws JsonProcessingException {
        var notificationDTO = objectMapper.readValue(payload, NotificationDTO.class);
        notificationServices.forEach(notificationService -> {
            try {
                notificationService.send(notificationDTO);
            } catch (NotificationException e) {
                log.error("Service {} failed to send notification : {}. Retryable? {}.", notificationService.getClass(), e.getMessage(), e.isRetryable());
                if (e.isRetryable()) {
                    throw e;
                }
            }
        });
    }

}
